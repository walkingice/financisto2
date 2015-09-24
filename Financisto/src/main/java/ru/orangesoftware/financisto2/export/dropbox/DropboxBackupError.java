package ru.orangesoftware.financisto2.export.dropbox;

public class DropboxBackupError {

    public final Exception e;

    public DropboxBackupError(Exception e) {
        this.e = e;
    }

}
