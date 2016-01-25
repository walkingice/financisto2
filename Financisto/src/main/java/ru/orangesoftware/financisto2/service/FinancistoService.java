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
package ru.orangesoftware.financisto2.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import java.util.Date;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.activity.AbstractTransactionActivity;
import ru.orangesoftware.financisto2.activity.AccountWidget;
import ru.orangesoftware.financisto2.activity.MassOpActivity;
import ru.orangesoftware.financisto2.backup.DatabaseExport;
import ru.orangesoftware.financisto2.blotter.BlotterFilter;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.export.Export;
import ru.orangesoftware.financisto2.filter.WhereFilter;
import ru.orangesoftware.financisto2.model.TransactionInfo;
import ru.orangesoftware.financisto2.model.TransactionStatus;
import ru.orangesoftware.financisto2.recur.NotificationOptions;
import ru.orangesoftware.financisto2.utils.MyPreferences;

import static ru.orangesoftware.financisto2.service.DailyAutoBackupScheduler.scheduleNextAutoBackup;

@EService
public class FinancistoService extends WakefulIntentService {

	private static final String TAG = "FinancistoService";
    public static final String ACTION_SCHEDULE_ALL = "ru.orangesoftware.financisto2.SCHEDULE_ALL";
    public static final String ACTION_SCHEDULE_ONE = "ru.orangesoftware.financisto2.SCHEDULE_ONE";
    public static final String ACTION_SCHEDULE_AUTO_BACKUP = "ru.orangesoftware.financisto2.ACTION_SCHEDULE_AUTO_BACKUP";
    public static final String ACTION_AUTO_BACKUP = "ru.orangesoftware.financisto2.ACTION_AUTO_BACKUP";

	private static final int RESTORED_NOTIFICATION_ID = 0;

    @Bean
	public DatabaseAdapter db;

    @Bean
    public RecurrenceScheduler scheduler;

    public FinancistoService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Created Financisto service ...");
    }

    @Override
	protected void doWakefulWork(Intent intent) {
        String action = intent.getAction();
        if (ACTION_SCHEDULE_ALL.equals(action)) {
            scheduleAll();
        } else if (ACTION_SCHEDULE_ONE.equals(action)) {
            scheduleOne(intent);
        } else if (ACTION_SCHEDULE_AUTO_BACKUP.equals(action)) {
            scheduleNextAutoBackup(this);
        } else if (ACTION_AUTO_BACKUP.equals(action)) {
            doAutoBackup();
        }
    }

    private void scheduleAll() {
        int restoredTransactionsCount = scheduler.scheduleAll(this);
        if (restoredTransactionsCount > 0) {
            notifyUser(createRestoredNotification(restoredTransactionsCount), RESTORED_NOTIFICATION_ID);
        }
    }

    private void scheduleOne(Intent intent) {
        long scheduledTransactionId = intent.getLongExtra(RecurrenceScheduler.SCHEDULED_TRANSACTION_ID, -1);
        if (scheduledTransactionId > 0) {
            TransactionInfo transaction = scheduler.scheduleOne(this, scheduledTransactionId);
            if (transaction != null) {
                notifyUser(transaction);
                AccountWidget.updateWidgets(this);
            }
        }
    }
    
    private void doAutoBackup() {
        try {
            try {
                long t0 = System.currentTimeMillis();
                Log.e(TAG, "Auto-backup started at " + new Date());
                DatabaseExport export = new DatabaseExport(this, db.db(), true);
                String fileName = export.export();
                if (MyPreferences.isDropboxUploadAutoBackups(this)) {
                    Export.uploadBackupFileToDropbox(this, fileName);
                }
                Log.e(TAG, "Auto-backup completed in " +(System.currentTimeMillis()-t0)+"ms");
            } catch (Exception e) {
                Log.e(TAG, "Auto-backup unsuccessful", e);
            }
        } finally {
            scheduleNextAutoBackup(this);
        }
    }

    private void notifyUser(TransactionInfo transaction) {
		Notification notification = createNotification(transaction);
		notifyUser(notification, (int)transaction.id);
	}

	private void notifyUser(Notification notification, int id) {
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(id, notification);		
	}

	private Notification createRestoredNotification(int count) {
		long when = System.currentTimeMillis();
        Context context = getApplicationContext();
		String text = getString(R.string.scheduled_transactions_have_been_restored, count);
        Intent notificationIntent = new Intent(this, MassOpActivity.class);
        WhereFilter filter = new WhereFilter("");
		filter.eq(BlotterFilter.STATUS, TransactionStatus.RS.name());
		filter.toIntent(notificationIntent);
        Notification notification = new Notification.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(getString(R.string.scheduled_transactions_restored))
                .setContentText(text)
                .setSmallIcon(R.drawable.notification_icon_transaction)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0))
                .build();

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		return notification;
	}

	private Notification createNotification(TransactionInfo t) {
		long when = System.currentTimeMillis();
        Context context = getApplicationContext();
        Intent notificationIntent = t.getActivityIntent(this);
        Notification notification = new Notification.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setTicker(t.getNotificationTickerText(this))
                .setContentTitle(t.getNotificationContentTitle(this))
                .setContentText(t.getNotificationContentText(this))
                .setSmallIcon(t.getNotificationIcon())
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0))
                .build();
        applyNotificationOptions(notification, t.notificationOptions);
		return notification;
	}

	private void applyNotificationOptions(Notification notification, String notificationOptions) {
		if (notificationOptions == null) {
			notification.defaults = Notification.DEFAULT_ALL;
		} else {
			NotificationOptions options = NotificationOptions.parse(notificationOptions);
			options.apply(notification);
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
