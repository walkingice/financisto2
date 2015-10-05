package ru.orangesoftware.financisto2.test.utils;

import android.test.AndroidTestCase;
import ru.orangesoftware.financisto2.model.Category;

import static ru.orangesoftware.financisto2.utils.TransactionTitleUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Denis Solonenko
 * Date: 2/18/11 7:15 PM
 */
public class TransactionTitleUtilsTest extends AndroidTestCase {

    public void test_should_generate_title_for_regular_transactions() {
        assertEquals("", generateTransactionTitle(sb(), null, null, Category.NO_CATEGORY_ID, null));
        assertEquals("Payee", generateTransactionTitle(sb(), "Payee", null, Category.NO_CATEGORY_ID, null));
        assertEquals("Note", generateTransactionTitle(sb(), null, "Note", Category.NO_CATEGORY_ID, null));
        assertEquals("Category", generateTransactionTitle(sb(), null, null, Category.NO_CATEGORY_ID, "Category"));
        assertEquals("Payee: Note", generateTransactionTitle(sb(), "Payee", "Note", Category.NO_CATEGORY_ID, null));
        assertEquals("Category (Payee: Note)", generateTransactionTitle(sb(), "Payee", "Note", Category.NO_CATEGORY_ID, "Category"));
    }

    public void test_should_generate_title_for_a_split() {
        assertEquals("[Split...]", generateTransactionTitle(sb(), null, null, Category.SPLIT_CATEGORY_ID, "[Split...]"));
        assertEquals("[Payee...]", generateTransactionTitle(sb(), "Payee", null, Category.SPLIT_CATEGORY_ID, "[Split...]"));
        assertEquals("[...] Note", generateTransactionTitle(sb(), null, "Note", Category.SPLIT_CATEGORY_ID, "[Split...]"));
        assertEquals("[...]", generateTransactionTitle(sb(), null, null, Category.SPLIT_CATEGORY_ID, "[Split...]"));
        assertEquals("[Payee...] Note", generateTransactionTitle(sb(), "Payee", "Note", Category.SPLIT_CATEGORY_ID, "[Split...]"));
    }

    private StringBuilder sb() {
        return new StringBuilder();
    }

}
