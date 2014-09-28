/*
 * Copyright (c) 2011 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */
package ru.orangesoftware.financisto2.backup;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.db.MyEntityManager;
import ru.orangesoftware.financisto2.service.RecurrenceScheduler;
import ru.orangesoftware.financisto2.service.RecurrenceScheduler_;
import ru.orangesoftware.financisto2.utils.CurrencyCache;
import ru.orangesoftware.financisto2.utils.IntegrityFix;

import static ru.orangesoftware.financisto2.backup.Backup.tableHasSystemIds;

public abstract class FullDatabaseImport {

	protected final Context context;
	protected final DatabaseAdapter dbAdapter;
    protected final MyEntityManager em;
	protected final SQLiteDatabase db;

	public FullDatabaseImport(Context context, DatabaseAdapter dbAdapter, MyEntityManager em) {
		this.context = context;
		this.dbAdapter = dbAdapter;
        this.em = em;
        this.db = dbAdapter.db();
    }

	public void importDatabase() throws IOException {
        db.beginTransaction();
        try {
            cleanDatabase();
            restoreDatabase();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        CurrencyCache.initialize(em);
        new IntegrityFix(dbAdapter).fix();
        scheduleAll();
    }

    protected abstract void restoreDatabase() throws IOException;

    private void cleanDatabase() {
        for (String tableName : tablesToClean()) {
            if (tableHasSystemIds(tableName) && shouldKeepSystemEntries()) {
                db.execSQL("delete from "+tableName+" where _id>0");
            } else {
                db.execSQL("delete from "+tableName);
            }
        }
    }

    protected boolean shouldKeepSystemEntries() {
        return false;
    }

    protected List<String> tablesToClean() {
        List<String> list = new ArrayList<String>(Arrays.asList(Backup.BACKUP_TABLES));
        list.add("running_balance");
        return list;
    }

    private void scheduleAll() {
        RecurrenceScheduler scheduler = RecurrenceScheduler_.getInstance_(context);
        scheduler.scheduleAll(context);
	}

}
