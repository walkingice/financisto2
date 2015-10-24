package ru.orangesoftware.financisto2.activity;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

import java.util.ArrayList;
import java.util.List;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.adapter.CategoryListAdapter;
import ru.orangesoftware.financisto2.adapter.MyEntityAdapter;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.db.DatabaseHelper.AttributeColumns;
import ru.orangesoftware.financisto2.db.DatabaseHelper.CategoryColumns;
import ru.orangesoftware.financisto2.fragment.BlotterFragment;
import ru.orangesoftware.financisto2.fragment.BlotterFragment_;
import ru.orangesoftware.financisto2.model.Account;
import ru.orangesoftware.financisto2.model.Attribute;
import ru.orangesoftware.financisto2.model.Category;
import ru.orangesoftware.financisto2.model.MyEntity;
import ru.orangesoftware.financisto2.utils.EnumUtils;
import ru.orangesoftware.financisto2.utils.LocalizableEnum;

import static ru.orangesoftware.financisto2.utils.Utils.checkEditText;
import static ru.orangesoftware.financisto2.utils.Utils.text;

@EActivity(R.layout.account_blotter)
public class AccountBlotterActivity extends FragmentActivity {

    @Bean
    protected DatabaseAdapter db;

    @Extra
    protected long accountId = -1;

    @AfterViews
    protected void afterViews() {
        Account account = db.getAccount(accountId);
        if (account != null) {
            setTitle(account.title);
            BlotterFragment fragment = BlotterFragment_.builder().saveFilter(false).accountId(accountId).build();
            getSupportFragmentManager().beginTransaction().add(R.id.layout, fragment).commit();
        }
    }

}
