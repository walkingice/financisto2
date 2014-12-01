package ru.orangesoftware.financisto2.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.model.Account;
import ru.orangesoftware.financisto2.model.Currency;
import ru.orangesoftware.financisto2.model.Transaction;
import ru.orangesoftware.financisto2.utils.CurrencyCache;
import ru.orangesoftware.financisto2.utils.MyPreferences;
import ru.orangesoftware.financisto2.utils.Utils;

import static ru.orangesoftware.financisto2.utils.Utils.text;

/**
 * Created by IntelliJ IDEA.
 * User: Denis Solonenko
 * Date: 4/21/11 7:17 PM
 */
@EActivity
public abstract class AbstractSplitActivity extends AbstractActivity {

    @Bean
    protected Utils utils;

    @Extra
    protected Transaction split;

    protected Account fromAccount;
    protected Currency originalCurrency;
    private ProjectSelector projectSelector;

    @ViewById(R.id.list)
    protected LinearLayout layout;

    protected EditText noteText;
    protected TextView unsplitAmountText;

    @AfterViews
    public void afterViews() {
        fetchData();
        projectSelector = new ProjectSelector(this, x);
        projectSelector.fetchProjects();

        if (split.fromAccountId > 0) {
            fromAccount = db.getAccount(split.fromAccountId);
        }
        if (split.originalCurrencyId > 0) {
            originalCurrency = CurrencyCache.getCurrency(db, split.originalCurrencyId);
        }

        createUI(layout);
        createCommonUI(layout);
        updateUI();
    }

    private void createCommonUI(LinearLayout layout) {
        unsplitAmountText = x.addInfoNode(layout, R.id.add_split, R.string.unsplit_amount, "0");

        noteText = new EditText(this);
        x.addEditNode(layout, R.string.note, noteText);

        projectSelector.createNode(layout);
    }

    protected abstract void fetchData();

    protected abstract void createUI(LinearLayout layout);

    @Override
    protected void onClick(View v, int id) {
        projectSelector.onClick(id);
    }

    @Override
    public void onSelectedPos(int id, int selectedPos) {
        projectSelector.onSelectedPos(id, selectedPos);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        projectSelector.onActivityResult(requestCode, resultCode, data);
    }

    @OptionsItem(R.id.menu_save)
    protected void saveAndFinish() {
        Intent data = new Intent();
        if (updateFromUI()) {
            split.toIntentAsSplit(data);
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    @OptionsItem(R.id.menu_cancel)
    protected void cancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    protected boolean updateFromUI() {
        split.note = text(noteText);
        split.projectId = projectSelector.getSelectedProjectId();
        return true;
    }

    protected void updateUI() {
        projectSelector.selectProject(split.projectId);
        setNote(split.note);
    }

    private void setNote(String note) {
        noteText.setText(note);
    }

    protected void setUnsplitAmount(long amount) {
        Currency currency = getCurrency();
        utils.setAmountText(unsplitAmountText, currency, amount, false);
    }

    protected Currency getCurrency() {
        return originalCurrency != null ? originalCurrency : (fromAccount != null ? fromAccount.currency : Currency.defaultCurrency());
    }

    @Override
    protected boolean shouldLock() {
        return MyPreferences.isPinProtectedNewTransaction(this);
    }

}
