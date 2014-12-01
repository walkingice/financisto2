package ru.orangesoftware.financisto2.activity;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;

import java.util.List;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.model.Account;
import ru.orangesoftware.financisto2.model.MyEntity;
import ru.orangesoftware.financisto2.utils.TransactionUtils;
import ru.orangesoftware.financisto2.widget.AmountInput;
import ru.orangesoftware.financisto2.widget.RateLayoutView;

/**
 * Created by IntelliJ IDEA.
 * User: Denis Solonenko
 * Date: 4/21/11 7:17 PM
 */
@EActivity(R.layout.split_fixed)
@OptionsMenu(R.menu.split_menu)
public class SplitTransferActivity extends AbstractSplitActivity {

    private RateLayoutView rateView;

    protected TextView accountText;
    protected List<Account> accounts;
    protected ListAdapter accountAdapter;

    @Override
    protected void createUI(LinearLayout layout) {
        accountText = x.addListNode(layout, R.id.account, R.string.account, R.string.select_to_account);
        rateView = new RateLayoutView(this, x, layout);
        rateView.createTransferUI();
        rateView.setAmountFromChangeListener(new AmountInput.OnAmountChangedListener() {
            @Override
            public void onAmountChanged(long oldAmount, long newAmount) {
                setUnsplitAmount(split.unsplitAmount - newAmount);
            }
        });
    }

    @Override
    protected void fetchData() {
        accounts = db.getAllAccountsList(true);
        accountAdapter = TransactionUtils.createAccountAdapter(this, accounts);
    }

    @Override
    protected void updateUI() {
        super.updateUI();
        selectFromAccount(split.fromAccountId);
        selectToAccount(split.toAccountId);
        setFromAmount(split.fromAmount);
        setToAmount(split.toAmount);
    }

    @Override
    protected boolean updateFromUI() {
        super.updateFromUI();
        split.fromAmount = rateView.getFromAmount();
        split.toAmount = rateView.getToAmount();
        if (split.fromAccountId == split.toAccountId) {
            Toast.makeText(this, R.string.select_to_account_differ_from_to_account, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void selectFromAccount(long accountId) {
        if (accountId > 0) {
            Account account = db.getAccount(accountId);
            rateView.selectCurrencyFrom(account.currency);
        }
    }

    private void selectToAccount(long accountId) {
        if (accountId > 0) {
            Account account = db.getAccount(accountId);
            rateView.selectCurrencyTo(account.currency);
            accountText.setText(account.title);
            split.toAccountId = accountId;
        }
    }

    private void setFromAmount(long amount) {
        rateView.setFromAmount(amount);
    }

    private void setToAmount(long amount) {
        rateView.setToAmount(amount);
    }

    @Override
    protected void onClick(View v, int id) {
        super.onClick(v, id);
        if (id == R.id.account) {
            int selectedPos = MyEntity.indexOf(accounts, split.toAccountId);
            x.selectItemId(this, R.id.account, R.string.account_to, accountAdapter, selectedPos);
        }
    }

    @Override
    public void onSelectedId(int id, long selectedId) {
        switch(id) {
            case R.id.account:
                selectToAccount(selectedId);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            rateView.onActivityResult(requestCode, data);
        }
    }

}
