package ru.orangesoftware.financisto2.app;

import android.support.multidex.MultiDexApplication;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;

import ru.orangesoftware.financisto2.bus.GreenRobotBus;
import ru.orangesoftware.financisto2.bus.UIEventHandler;
import ru.orangesoftware.financisto2.export.drive.GoogleDriveClient;
import ru.orangesoftware.financisto2.export.dropbox.Dropbox;

@EApplication
public class FinancistoApp extends MultiDexApplication {

    @Bean
    public GreenRobotBus bus;

    @Bean
    public UIEventHandler handler;

    @Bean
    public GoogleDriveClient driveClient;

    @Bean
    public Dropbox dropboxClient;

    @AfterInject
    public void init() {
        bus.register(handler);
        bus.register(driveClient);
        bus.register(dropboxClient);
    }

}
