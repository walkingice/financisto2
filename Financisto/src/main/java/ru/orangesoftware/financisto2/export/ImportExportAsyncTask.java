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
package ru.orangesoftware.financisto2.export;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.db.CategoryRepository;
import ru.orangesoftware.financisto2.db.CategoryRepository_;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.db.DatabaseAdapter_;
import ru.orangesoftware.financisto2.export.csv.Csv;
import ru.orangesoftware.financisto2.utils.MyPreferences;

import static ru.orangesoftware.financisto2.export.Export.uploadBackupFileToDropbox;

public abstract class ImportExportAsyncTask extends AsyncTask<String, String, Object> {
	
	protected final Context context;
	protected final ProgressDialog dialog;
    private boolean showResultDialog = true;

    private ImportExportAsyncTaskListener listener;
	
	public ImportExportAsyncTask(Context context, ProgressDialog dialog) {
		this.dialog = dialog;
		this.context = context;
	}

    public void setListener(ImportExportAsyncTaskListener listener) {
        this.listener = listener;
    }

    public void setShowResultDialog(boolean showResultDialog) {
        this.showResultDialog = showResultDialog;
    }

    @Override
	protected Object doInBackground(String... params) {
		DatabaseAdapter db = DatabaseAdapter_.getInstance_(context);
        CategoryRepository categoryRepository = CategoryRepository_.getInstance_(context);
        try {
			return work(context, db, categoryRepository, params);
		} catch(Exception ex){
			Log.e("Financisto", "Unable to do import/export", ex);
			return ex;
		}
	}

	protected abstract Object work(Context context, DatabaseAdapter db, CategoryRepository categoryRepository, String...params) throws Exception;
	
	protected abstract String getSuccessMessage(Object result);

    protected void doUploadToDropbox(Context context, String backupFileName) throws Exception {
        if (MyPreferences.isDropboxUploadBackups(context)) {
            doForceUploadToDropbox(context, backupFileName);
        }
    }

    protected void doForceUploadToDropbox(Context context, String backupFileName) throws Exception {
        publishProgress(context.getString(R.string.dropbox_uploading_file));
        uploadBackupFileToDropbox(context, backupFileName);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        dialog.setMessage(values[0]);
    }

	@Override
	protected void onPostExecute(Object result) {
		dialog.dismiss();

		if (result instanceof Exception) {
            Exception exception = (Exception) result;
            new AlertDialog.Builder(context)
                    .setTitle(R.string.fail)
                    .setMessage(exception.getMessage())
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return;
        }

		String message = getSuccessMessage(result);

        if (listener != null) {
            listener.onCompleted();
        }

        if (showResultDialog) {
            new AlertDialog.Builder(context)
                .setTitle(R.string.success)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
        }
	}
	
}

