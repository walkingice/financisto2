/*******************************************************************************
 * Copyright (c) 2010 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Denis Solonenko - initial API and implementation
 ******************************************************************************/
package ru.orangesoftware.financisto2.backup;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.DriveContents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

import ru.orangesoftware.financisto2.db.CategoryRepository;
import ru.orangesoftware.financisto2.db.Database;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.db.DatabaseSchemaEvolution;
import ru.orangesoftware.financisto2.export.Export;
import ru.orangesoftware.financisto2.export.dropbox.Dropbox;
import ru.orangesoftware.financisto2.model.AccountType;
import ru.orangesoftware.financisto2.model.ElectronicPaymentType;

public class DatabaseImport extends FullDatabaseImport {

    private static final int FINANCISTO1_VERSION = 100;

    private final DatabaseSchemaEvolution schemaEvolution;
    private final InputStream backupStream;

    public static DatabaseImport createFromFileBackup(Context context, DatabaseAdapter db, CategoryRepository categoryRepository, String backupFile) throws FileNotFoundException {
        File backupPath = Export.getBackupFolder(context);
        File file = new File(backupPath, backupFile);
        FileInputStream inputStream = new FileInputStream(file);
        return new DatabaseImport(context, db, categoryRepository, inputStream);
    }

    public static DatabaseImport createFromGoogleDriveBackup(Context context, DatabaseAdapter db, CategoryRepository categoryRepository, DriveContents driveFileContents)
            throws IOException {
        InputStream inputStream = driveFileContents.getInputStream();
        InputStream in = new GZIPInputStream(inputStream);
        return new DatabaseImport(context, db, categoryRepository, in);
    }

    public static DatabaseImport createFromDropboxBackup(Context context, DatabaseAdapter db, CategoryRepository categoryRepository, Dropbox dropbox, String backupFile)
            throws Exception {
        InputStream inputStream = dropbox.getFileAsStream(backupFile);
        InputStream in = new GZIPInputStream(inputStream);
        return new DatabaseImport(context, db, categoryRepository, in);
    }

    private DatabaseImport(Context context, DatabaseAdapter db, CategoryRepository categoryRepository, InputStream backupStream) {
        super(context, db, categoryRepository);
        this.schemaEvolution = new DatabaseSchemaEvolution(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION);
        this.backupStream = backupStream;
	}

    @Override
    protected void restoreDatabase() throws IOException {
        InputStream s = decompressStream(backupStream);
        InputStreamReader isr = new InputStreamReader(s, "UTF-8");
        BufferedReader br = new BufferedReader(isr, 65535);
        try {
            recoverDatabase(br);
        } finally {
            br.close();
        }
    }

    private InputStream decompressStream(InputStream input) throws IOException {
        PushbackInputStream pb = new PushbackInputStream(input, 2);
        byte[] bytes = new byte[2];
        pb.read(bytes);
        pb.unread(bytes);
        int head = ((int) bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00);
        if (GZIPInputStream.GZIP_MAGIC == head) {
            return new GZIPInputStream(pb);
        } else {
            return pb;
        }
    }

    private void recoverDatabase(BufferedReader br) throws IOException {
        boolean insideEntity = false;
        ContentValues values = new ContentValues();
        String line;
        String tableName = null;
        boolean isFinancisto1 = false;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("VERSION_CODE:")) {
                int i = line.indexOf(":");
                if (i > 0) {
                    String value = line.substring(i + 1);
                    int version = Integer.parseInt(value);
                    isFinancisto1 = version <= FINANCISTO1_VERSION;
                }
            } else if (line.startsWith("$")) {
                if ("$$".equals(line)) {
                    if (tableName != null && values.size() > 0) {
                        if (shouldRestoreTable(tableName)) {
                            cleanupValues(tableName, values);
                            if (values.size() > 0) {
                                sqlDb.insert(tableName, null, values);
                            }
                        }
                        tableName = null;
                        insideEntity = false;
                    }
                } else {
                    int i = line.indexOf(":");
                    if (i > 0) {
                        tableName = line.substring(i+1);
                        insideEntity = true;
                        values.clear();
                    }
                }
            } else {
                if (insideEntity) {
                    int i = line.indexOf(":");
                    if (i > 0) {
                        String columnName = line.substring(0, i);
                        String value = line.substring(i+1);
                        values.put(columnName, value);
                    }
                }
            }
        }
	}

    private boolean shouldRestoreTable(String tableName) {
        if ("locations".equals(tableName)) return false;
        return true;
    }

    private void cleanupValues(String tableName, ContentValues values) {
        // remove system entities
        Integer id = values.getAsInteger("_id");
        if (id != null && id <= 0) {
            Log.w("Financisto", "Removing system entity: "+values);
            values.clear();
            return;
        }
        // fix columns
        if ("transactions".equals(tableName)) {
            values.remove("location_id");
            values.remove("provider");
            values.remove("accuracy");
            values.remove("latitude");
            values.remove("longitude");
        } else if ("category".equals(tableName)) {
            values.remove("last_location_id");
            values.remove("sort_order");
        } else if ("account".equals(tableName)) {
            String type = values.getAsString("type");
            if ("PAYPAL".equals(type)) {
                values.put("type", AccountType.ELECTRONIC.name());
                values.put("card_issuer", ElectronicPaymentType.PAYPAL.name());
            }
        }
    }

}
