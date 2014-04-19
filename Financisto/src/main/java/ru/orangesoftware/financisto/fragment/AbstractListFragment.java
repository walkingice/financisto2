package ru.orangesoftware.financisto.fragment;

import android.support.v4.app.ListFragment;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import ru.orangesoftware.financisto.bus.GreenRobotBus;

@EFragment
public abstract class AbstractListFragment extends ListFragment {

    @Bean
    public GreenRobotBus bus;

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

}
