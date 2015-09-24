/*
 * Copyright (c) 2012 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package ru.orangesoftware.financisto2.export.dropbox;

import android.content.Context;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.backup.DatabaseExport;
import ru.orangesoftware.financisto2.backup.DatabaseImport;
import ru.orangesoftware.financisto2.bus.GreenRobotBus;
import ru.orangesoftware.financisto2.db.CategoryRepository;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.export.ImportExportException;
import ru.orangesoftware.financisto2.export.drive.DoDriveBackup;
import ru.orangesoftware.financisto2.utils.MyPreferences;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@EBean(scope = EBean.Scope.Singleton)
public class Dropbox {

    public static final String APP_KEY = "INSERT_APP_KEY_HERE";
    public static final String APP_SECRET = "INSERT_APP_SECRET_HERE";
    public static final Session.AccessType ACCESS_TYPE = Session.AccessType.APP_FOLDER;

    private final Context context;
    private final DropboxAPI<AndroidAuthSession> dropboxApi;

    @Bean
    GreenRobotBus bus;

    @Bean
    DatabaseAdapter db;

    @Bean
    CategoryRepository categoryRepository;

    private boolean startedAuth = false;

    public Dropbox(Context context) {
        this.context = context;
        this.dropboxApi = createApi();
    }

    @AfterInject
    public void init() {
        bus.register(this);
    }

    public void startAuth() {
        startedAuth = true;
        dropboxApi.getSession().startOAuth2Authentication(context);
    }

    public void completeAuth() {
        try {
            if (startedAuth && dropboxApi.getSession().authenticationSuccessful()) {
                try {
                    dropboxApi.getSession().finishAuthentication();
                    String token = dropboxApi.getSession().getOAuth2AccessToken();
                    MyPreferences.storeDropboxToken(context, token);
                } catch (IllegalStateException e) {
                    Log.i("Financisto", "Error authenticating Dropbox", e);
                }
            }
        } finally {
            startedAuth = false;
        }
    }

    public void deAuth() {
        MyPreferences.removeDropboxKeys(context);
        dropboxApi.getSession().unlink();
    }

    private DropboxAPI<AndroidAuthSession> createApi() {
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
        return new DropboxAPI<AndroidAuthSession>(session);
    }

    public boolean authSession() {
        String token = MyPreferences.getDropboxToken(context);
        if (token != null) {
            dropboxApi.getSession().setOAuth2AccessToken(token);
            return dropboxApi.getSession().isLinked();
        }
        return false;
    }

    public void uploadFile(File file) throws Exception {
        InputStream is = new FileInputStream(file);
        uploadStream(file.getName(), is, file.length());
    }

    public void uploadStream(String fileName, InputStream is, long length) throws Exception {
        try {
            if (authSession()) {
                try {
                    DropboxAPI.Entry newEntry = dropboxApi.putFile(fileName, is, length, null, null);
                    Log.i("Financisto", "Dropbox: The uploaded file's rev is: " + newEntry.rev);
                } catch (Exception e) {
                    Log.e("Financisto", "Dropbox: Something wrong", e);
                    throw new ImportExportException(context.getString(R.string.dropbox_error), e);
                }
            } else {
                throw new ImportExportException(context.getString(R.string.dropbox_auth_error));
            }
        } finally {
            is.close();
        }
    }

    public List<String> listFiles() throws Exception {
        if (authSession()) {
            try {
                List<String> files = new ArrayList<String>();
                List<DropboxAPI.Entry> entries = dropboxApi.search("/", ".backup", 1000, false);
                for (DropboxAPI.Entry entry : entries) {
                    if (entry.fileName() != null) {
                        files.add(entry.fileName());
                    }
                }
                Collections.sort(files, new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        return s2.compareTo(s1);
                    }
                });
                return files;
            } catch (Exception e) {
                Log.e("Financisto", "Dropbox: Something wrong", e);
                throw new ImportExportException(context.getString(R.string.dropbox_error), e);
            }
        } else {
            throw new ImportExportException(context.getString(R.string.dropbox_auth_error));
        }
    }

    public InputStream getFileAsStream(String backupFile) throws Exception {
        if (authSession()) {
            try {
                return dropboxApi.getFileStream("/" + backupFile, null);
            } catch (Exception e) {
                Log.e("Financisto", "Dropbox: Something wrong", e);
                throw new ImportExportException(context.getString(R.string.dropbox_error), e);
            }
        } else {
            throw new ImportExportException(context.getString(R.string.dropbox_auth_error));
        }
    }

    public void onEventBackgroundThread(DoDropboxBackup event) {
        DatabaseExport export = new DatabaseExport(context, db.db(), true);
        try {
            String fileName = export.generateFilename();
            byte[] backupBytes = export.generateBackupBytes();
            InputStream is = new ByteArrayInputStream(backupBytes);
            uploadStream(fileName, is, backupBytes.length);
            handleSuccess(fileName);
        } catch (Exception e) {
            handleError(e);
        }
    }

    public void onEventBackgroundThread(DoDropboxListFiles event) {
        try {
            List<String> files = listFiles();
            bus.post(new DropboxFileList(files));
        } catch (Exception e) {
            handleError(e);
        }
    }

    public void onEventBackgroundThread(DoDropboxRestore event) {
        try {
            DatabaseImport.createFromDropboxBackup(context, db, categoryRepository, this, event.backupFile).importDatabase();
            bus.post(new DropboxRestoreSuccess());
        } catch (Exception e) {
            handleError(e);
        }

    }

    private void handleSuccess(String fileName) {
        bus.post(new DropboxBackupSuccess(fileName));
    }

    private void handleError(Exception e) {
        bus.post(new DropboxBackupError(e));
    }

}
