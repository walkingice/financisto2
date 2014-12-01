package ru.orangesoftware.financisto2.test.report;

import java.util.List;
import java.util.Map;

import ru.orangesoftware.financisto2.filter.WhereFilter;
import ru.orangesoftware.financisto2.graph.GraphUnit;
import ru.orangesoftware.financisto2.model.Account;
import ru.orangesoftware.financisto2.model.Category;
import ru.orangesoftware.financisto2.model.Currency;
import ru.orangesoftware.financisto2.report.IncomeExpense;
import ru.orangesoftware.financisto2.report.Report;
import ru.orangesoftware.financisto2.report.ReportData;
import ru.orangesoftware.financisto2.test.builders.AccountBuilder;
import ru.orangesoftware.financisto2.test.builders.CategoryBuilder;
import ru.orangesoftware.financisto2.test.builders.CurrencyBuilder;
import ru.orangesoftware.financisto2.test.db.AbstractDbTest;
import ru.orangesoftware.financisto2.utils.CurrencyCache;

/**
 * Created by IntelliJ IDEA.
 * User: Denis Solonenko
 * Date: 7/8/11 1:26 AM
 */
public abstract class AbstractReportTest extends AbstractDbTest {

    Currency c1;
    Currency c2;
    Account a1;
    Account a2;
    Account a3;
    Report report;
    Map<String, Category> categories;
    WhereFilter filter = WhereFilter.empty();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        c1 = CurrencyBuilder.withDb(db).name("USD").title("Dollar").symbol("$").makeDefault().create();
        c2 = CurrencyBuilder.withDb(db).name("SGD").title("Singapore Dollar").symbol("S$").create();
        a1 = AccountBuilder.createDefault(db, c1);
        a2 = AccountBuilder.createDefault(db, c1);
        a3 = AccountBuilder.createDefault(db, c2);
        categories = CategoryBuilder.createDefaultHierarchy(db);
        report = createReport();
        CurrencyCache.initialize(db);
    }

    protected abstract Report createReport();

    List<GraphUnit> assertReportReturnsData() {
        return assertReportReturnsData(IncomeExpense.BOTH);
    }

    List<GraphUnit> assertReportReturnsData(IncomeExpense incomeExpense) {
        report.setIncomeExpense(incomeExpense);
        ReportData data = report.getReport(db, filter);
        assertNotNull(data);
        List<GraphUnit> units = data.units;
        assertNotNull(units);
        return units;
    }

    void assertName(GraphUnit unit, String name) {
        assertEquals(name, unit.name);
    }

    void assertIncome(GraphUnit u, long amount) {
        assertEquals(amount, u.getIncomeExpense().income.longValue());
    }

    void assertExpense(GraphUnit u, long amount) {
        assertEquals(amount, u.getIncomeExpense().expense.longValue());
    }

}
