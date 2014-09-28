/*
 * Copyright (c) 2011 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package ru.orangesoftware.financisto2.export.qif;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.activity.MainActivity;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.db.MyEntityManager;
import ru.orangesoftware.financisto2.export.ImportExportAsyncTask;
import ru.orangesoftware.financisto2.export.ImportExportAsyncTaskListener;

/**
 * Created by IntelliJ IDEA.
 * User: Denis Solonenko
 * Date: 11/7/11 10:45 PM
 */
public class QifImportTask extends ImportExportAsyncTask {

    private final QifImportOptions options;
    private final Handler handler;

    public QifImportTask(final MainActivity mainActivity, Handler handler, ProgressDialog dialog, QifImportOptions options) {
        super(mainActivity, dialog);
        this.options = options;
        this.handler = handler;
        setListener(new ImportExportAsyncTaskListener() {
            @Override
            public void onCompleted() {
                mainActivity.onTabChanged(mainActivity.getTabHost().getCurrentTabTag());
            }
        });
    }

    @Override
    protected Object work(Context context, DatabaseAdapter db, MyEntityManager em, String... params) throws Exception {
        try {
            QifImport qifImport = new QifImport(context, db, em, options);
            qifImport.importDatabase();
            return null;
        } catch (Exception e) {
            Log.e("Financisto", "Qif import error", e);
            handler.sendEmptyMessage(R.string.qif_import_error);
            return e;
        }
    }

    @Override
    protected String getSuccessMessage(Object result) {
        return context.getString(R.string.qif_import_success);
    }

}
