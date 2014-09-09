package ru.orangesoftware.financisto.export.drive;

import com.google.android.gms.common.ConnectionResult;

public class DriveBackupError {

    public final Exception e;

    public DriveBackupError(Exception e) {
        this.e = e;
    }

}
