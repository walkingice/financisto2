package ru.orangesoftware.financisto.app;

import android.app.Application;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;

import ru.orangesoftware.financisto.bus.GreenRobotBus;
import ru.orangesoftware.financisto.bus.UIEventHandler;
import ru.orangesoftware.financisto.export.drive.GoogleDriveClient;

@EApplication
public class FinancistoApp extends Application {

    @Bean
    public GreenRobotBus bus;

    @Bean
    public UIEventHandler handler;

    @Bean
    public GoogleDriveClient driveClient;

    @AfterInject
    public void init() {
        bus.register(handler);
        bus.register(driveClient);
    }

}
