package ru.orangesoftware.financisto2.test.model;

import java.util.Map;

import ru.orangesoftware.financisto2.model.Account;
import ru.orangesoftware.financisto2.model.Budget;
import ru.orangesoftware.financisto2.model.Category;
import ru.orangesoftware.financisto2.model.MyEntity;
import ru.orangesoftware.financisto2.model.Project;
import ru.orangesoftware.financisto2.model.Transaction;
import ru.orangesoftware.financisto2.test.builders.AccountBuilder;
import ru.orangesoftware.financisto2.test.builders.CategoryBuilder;
import ru.orangesoftware.financisto2.test.builders.DateTime;
import ru.orangesoftware.financisto2.test.builders.TransactionBuilder;
import ru.orangesoftware.financisto2.test.db.AbstractDbTest;

/**
 * Created by IntelliJ IDEA.
 * User: Denis Solonenko
 * Date: 4/28/11 11:05 PM
 */
public class BudgetTest extends AbstractDbTest {

    Budget budgetOne;
    Account account;
    Project project;
    Map<String, Category> categoriesMap;
    Map<Long, Category> categories;
    Map<Long, Project> projects;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        account = AccountBuilder.createDefault(db);
        categoriesMap = CategoryBuilder.createDefaultHierarchy(db);
        categories = MyEntity.asMap(db.getCategoriesList(true));
        project = new Project();
        project.title = "P1";
        db.saveOrUpdate(project);
        projects = MyEntity.asMap(db.getAllProjectsList(true));
        createBudget();
    }

    private void createBudget() {
        budgetOne = new Budget();
        budgetOne.currency = account.currency;
        budgetOne.amount = 1000;
        budgetOne.categories = String.valueOf(categoriesMap.get("A").id);
        budgetOne.projects = String.valueOf(project.id);
        budgetOne.expanded = true;
        budgetOne.includeSubcategories = true;
        budgetOne.startDate = DateTime.date(2011, 4, 1).atMidnight().asLong();
        budgetOne.endDate = DateTime.date(2011, 4, 30).at(23, 59, 59, 999).asLong();
        db.saveOrUpdate(budgetOne);
    }

    public void test_should_calculate_budget_correctly_with_regular_transactions() {
        // zero initially
        long spent = db.fetchBudgetBalance(categories, projects, budgetOne);
        assertEquals(0, spent);
        // yes, should affect budget
        TransactionBuilder.withDb(db).account(account).dateTime(DateTime.date(2011, 4, 1).atNoon()).amount(-100).category(categoriesMap.get("A")).create();
        spent = db.fetchBudgetBalance(categories, projects, budgetOne);
        assertEquals(-100, spent);
        // no, period is out
        TransactionBuilder.withDb(db).account(account).dateTime(DateTime.date(2011, 5, 1).atNoon()).amount(-200).category(categoriesMap.get("A")).create();
        spent = db.fetchBudgetBalance(categories, projects, budgetOne);
        assertEquals(-100, spent);
        // no, category is out
        TransactionBuilder.withDb(db).account(account).dateTime(DateTime.date(2011, 4, 1).atNoon()).amount(-200).category(categoriesMap.get("B")).create();
        spent = db.fetchBudgetBalance(categories, projects, budgetOne);
        assertEquals(-100, spent);
        // yes, child category
        TransactionBuilder.withDb(db).account(account).dateTime(DateTime.date(2011, 4, 2).atNoon()).amount(-200).category(categoriesMap.get("A1")).create();
        spent = db.fetchBudgetBalance(categories, projects, budgetOne);
        assertEquals(-300, spent);
    }

    public void test_should_calculate_budget_correctly_with_splits() {
        // zero initially
        long spent = db.fetchBudgetBalance(categories, projects, budgetOne);
        assertEquals(0, spent);
        // yes, should affect budget
        Transaction t = TransactionBuilder.withDb(db).account(account).dateTime(DateTime.date(2011, 4, 1).atNoon())
                .amount(-100)
                .category(CategoryBuilder.split(db))
                .withSplit(categoriesMap.get("A1"), -60)
                .withSplit(categoriesMap.get("B"), -30)
                .withSplit(categoriesMap.get("B"), project, -10)
                .create();
        spent = db.fetchBudgetBalance(categories, projects, budgetOne);
        assertEquals(-70, spent);
        // back to zero when split gets deleted
        db.deleteTransaction(t.id);
        spent = db.fetchBudgetBalance(categories, projects, budgetOne);
        assertEquals(0, spent);
    }

    public void test_should_calculate_budget_total() {

    }

}
