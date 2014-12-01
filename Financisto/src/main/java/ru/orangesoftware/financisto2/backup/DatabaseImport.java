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

import com.google.android.gms.drive.Contents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

import ru.orangesoftware.financisto2.db.Database;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.db.DatabaseSchemaEvolution;
import ru.orangesoftware.financisto2.db.MyEntityManager;
import ru.orangesoftware.financisto2.export.Export;
import ru.orangesoftware.financisto2.export.dropbox.Dropbox;

public class DatabaseImport extends FullDatabaseImport {

	private final DatabaseSchemaEvolution schemaEvolution;
    private final InputStream backupStream;

    public static DatabaseImport createFromFileBackup(Context context, DatabaseAdapter db, MyEntityManager em, String backupFile) throws FileNotFoundException {
        File backupPath = Export.getBackupFolder(context);
        File file = new File(backupPath, backupFile);
        FileInputStream inputStream = new FileInputStream(file);
        return new DatabaseImport(context, db, em, inputStream);
    }

    public static DatabaseImport createFromGoogleDriveBackup(Context context, DatabaseAdapter db, MyEntityManager em, Contents driveFileContents)
            throws IOException {
        InputStream inputStream = driveFileContents.getInputStream();
        InputStream in = new GZIPInputStream(inputStream);
        return new DatabaseImport(context, db, em, in);
    }

    public static DatabaseImport createFromDropboxBackup(Context context, DatabaseAdapter db, MyEntityManager em, Dropbox dropbox, String backupFile)
            throws Exception {
        InputStream inputStream = dropbox.getFileAsStream(backupFile);
        InputStream in = new GZIPInputStream(inputStream);
        return new DatabaseImport(context, db, em, in);
    }

    private DatabaseImport(Context context, DatabaseAdapter db, MyEntityManager em, InputStream backupStream) {
        super(context, db, em);
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
        if (GZIPInputStream.GZIP_MAGIC == head)
            return new GZIPInputStream(pb);
        else
            return pb;
    }

    private void recoverDatabase(BufferedReader br) throws IOException {
        boolean insideEntity = false;
        ContentValues values = new ContentValues();
        String line;
        String tableName = null;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("$")) {
                if ("$$".equals(line)) {
                    if (tableName != null && values.size() > 0) {
                        db.insert(tableName, null, values);
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

}
