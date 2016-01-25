package ru.orangesoftware.financisto2.fragment;

import android.support.v4.app.Fragment;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import ru.orangesoftware.financisto2.bus.GreenRobotBus;

@EFragment
public abstract class AbstractFragment extends Fragment {

    @Bean
    public GreenRobotBus bus;

    @AfterInject
    public void afterInject() {
        bus.register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
        reload();
    }

    protected abstract void reload();

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

}
