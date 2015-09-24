package ru.orangesoftware.financisto2.export.dropbox;

import java.util.List;

import ru.orangesoftware.financisto2.export.drive.DriveFileInfo;

public class DropboxFileList {

    public final List<String> files;

    public DropboxFileList(List<String> files) {
        this.files = files;
    }

}
