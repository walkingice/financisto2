/*
 * Copyright (c) 2014 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package ru.orangesoftware.financisto2.export.dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.activity.MainActivity;
import ru.orangesoftware.financisto2.backup.DatabaseImport;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.db.MyEntityManager;
import ru.orangesoftware.financisto2.export.ImportExportAsyncTask;
import ru.orangesoftware.financisto2.export.ImportExportAsyncTaskListener;

/**
 * Created by IntelliJ IDEA.
 * User: Denis Solonenko
 * Date: 11/9/11 2:16 AM
 */
public class DropboxRestoreTask extends ImportExportAsyncTask {

    private final String backupFile;

    public DropboxRestoreTask(final MainActivity mainActivity, ProgressDialog dialog, String backupFile) {
        super(mainActivity, dialog);
        setListener(new ImportExportAsyncTaskListener() {
            @Override
            public void onCompleted() {
                mainActivity.onTabChanged(mainActivity.getTabHost().getCurrentTabTag());
            }
        });
        this.backupFile = backupFile;
    }

    @Override
    protected Object work(Context context, DatabaseAdapter db, MyEntityManager em, String... params) throws Exception {
        Dropbox dropbox = new Dropbox(context);
        DatabaseImport.createFromDropboxBackup(context, db, em, dropbox, backupFile).importDatabase();
        return true;
    }

    @Override
    protected String getSuccessMessage(Object result) {
        return context.getString(R.string.restore_database_success);
    }

}
