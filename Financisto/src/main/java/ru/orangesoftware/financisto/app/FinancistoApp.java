package ru.orangesoftware.financisto.app;

import android.app.Application;
import android.content.Context;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;

import ru.orangesoftware.financisto.bus.GreenRobotBus;
import ru.orangesoftware.financisto.bus.UIEventHandler;
import ru.orangesoftware.financisto.db.DatabaseAdapter;

@EApplication
public class FinancistoApp extends Application {

    @Bean
    public GreenRobotBus bus;

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        DatabaseAdapter db = new DatabaseAdapter(context);
        bus.register(new UIEventHandler(context, db, bus));
    }

}
