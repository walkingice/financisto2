/*******************************************************************************
 * Copyright (c) 2010 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Denis Solonenko - initial API and implementation
 ******************************************************************************/
package ru.orangesoftware.financisto2.export.csv;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.util.LongSparseArray;

import ru.orangesoftware.financisto2.db.CategoryRepository;
import ru.orangesoftware.financisto2.db.CategoryRepository_;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.export.Export;
import ru.orangesoftware.financisto2.model.*;
import ru.orangesoftware.financisto2.utils.CurrencyCache;
import ru.orangesoftware.financisto2.utils.Utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static ru.orangesoftware.financisto2.datetime.DateUtils.FORMAT_DATE_ISO_8601;
import static ru.orangesoftware.financisto2.datetime.DateUtils.FORMAT_TIME_ISO_8601;

public class CsvExport extends Export {

    public static final String[] HEADER = "date,time,account,amount,currency,original amount,original currency,category,parent,payee,location,project,note".split(",");

    private static final MyLocation TRANSFER_IN = new MyLocation();
    private static final MyLocation TRANSFER_OUT = new MyLocation();

    static {
        TRANSFER_IN.name = "Transfer In";
        TRANSFER_OUT.name = "Transfer Out";
    }

    private final DatabaseAdapter db;
	private final CategoryRepository categoryRepository;
    private final CsvExportOptions options;

    private LongSparseArray<Category> categoriesMap;
    private LongSparseArray<Account> accountsMap;
    private LongSparseArray<Payee> payeeMap;
    private LongSparseArray<Project> projectMap;
    private LongSparseArray<MyLocation> locationMap;

    public CsvExport(Context context, DatabaseAdapter db, CategoryRepository categoryRepository, CsvExportOptions options) {
        super(context, false);
		this.db = db;
        this.categoryRepository = categoryRepository;
        this.options = options;
	}
	
	@Override
	protected String getExtension() {
		return ".csv";
	}

	@Override
	protected void writeHeader(BufferedWriter bw) throws IOException  {
        if (options.writeUtfBom) {
            byte[] bom = new byte[3];
            bom[0] = (byte) 0xEF;
            bom[1] = (byte) 0xBB;
            bom[2] = (byte) 0xBF;
            bw.write(new String(bom,"UTF-8"));
        }
		if (options.includeHeader) {
			Csv.Writer w = new Csv.Writer(bw).delimiter(options.fieldSeparator);
            for (String h : HEADER) {
                w.value(h);
            }
			w.newLine();
		}
	}

	@Override
	protected void writeBody(BufferedWriter bw) throws IOException {
		Csv.Writer w = new Csv.Writer(bw).delimiter(options.fieldSeparator);
		try {
            accountsMap = db.getAllAccountsMap();
            categoriesMap = categoryRepository.loadCategories().asIdMap();
            payeeMap = db.getAllPayeeByIdMap();
            projectMap = db.getAllProjectsByIdMap(true);
            locationMap = db.getAllLocationsByIdMap(false);
            Cursor c = db.getBlotter(options.filter);
			try {			
				while (c.moveToNext()) {
                    Transaction t = Transaction.fromBlotterCursor(c);
					writeLine(w, t);
				}					
			} finally {
				c.close();
			}
		} finally {
			w.close();
		}
	}

    private void writeLine(Csv.Writer w, Transaction t) {
        Date dt = t.dateTime > 0 ? new Date(t.dateTime) : null;
        Category category = getCategoryById(t.categoryId);
        Project project = getProjectById(t.projectId);
        Account fromAccount = getAccount(t.fromAccountId);
        if (t.isTransfer()) {
            Account toAccount = getAccount(t.toAccountId);
            writeLine(w, dt, fromAccount.title, t.fromAmount, fromAccount.currency.id, 0, 0, category, null, TRANSFER_OUT, project, t.note);
            writeLine(w, dt, toAccount.title, t.toAmount, toAccount.currency.id, 0, 0, category, null, TRANSFER_IN, project, t.note);
        } else {
            MyLocation location = getLocationById(t.locationId);
            Payee payee = getPayee(t.payeeId);
            writeLine(w, dt, fromAccount.title, t.fromAmount, fromAccount.currency.id, t.originalFromAmount, t.originalCurrencyId,
                    category, payee, location, project, t.note);
            if (category != null && category.isSplit() && options.exportSplits) {
                List<Transaction> splits = db.getSplitsForTransaction(t.id);
                for (Transaction split : splits) {
                    split.dateTime = 0;
                    writeLine(w, split);
                }
            }
        }
    }

    private void writeLine(Csv.Writer w, Date dt, String account,
                           long amount, long currencyId,
                           long originalAmount, long originalCurrencyId,
			               Category category, Payee payee, MyLocation location, Project project, String note) {
        if (dt != null) {
		    w.value(FORMAT_DATE_ISO_8601.format(dt));
		    w.value(FORMAT_TIME_ISO_8601.format(dt));
        } else {
            w.value("~");
            w.value("");
        }
		w.value(account);
        String amountFormatted = options.amountFormat.format(new BigDecimal(amount).divide(Utils.HUNDRED));
        w.value(amountFormatted);
		Currency c = CurrencyCache.getCurrency(db, currencyId);
		w.value(c.name);
        if (originalCurrencyId > 0) {
            w.value(options.amountFormat.format(new BigDecimal(originalAmount).divide(Utils.HUNDRED)));
            Currency originalCurrency = CurrencyCache.getCurrency(db, originalCurrencyId);
            w.value(originalCurrency.name);
        } else {
            w.value("");
            w.value("");
        }
		w.value(category != null ? category.title : "");
		String sParent = buildPath(category);
		w.value(sParent);
        w.value(payee != null ? payee.title : "");
		w.value(location != null ? location.name : "");
		w.value(project != null ? project.title : "");
		w.value(note);
		w.newLine();
	}

	private String buildPath(Category category) {
		if (category == null || category.parent == null) {
			return "";
		} else {
            StringBuilder sb = new StringBuilder(category.parent.title);
			for (Category cat = category.parent.parent; cat != null && cat.id > 0; cat = cat.parent) {
                sb.insert(0,":").insert(0, cat.title);
			}
			return sb.toString();
		}
	}

	@Override
	protected void writeFooter(BufferedWriter bw) throws IOException {
	}

    private Account getAccount(long accountId) {
        return accountsMap.get(accountId);
    }

	public Category getCategoryById(long id) {
        if (id == 0) return null;
        if (id == -1) {
            Category c = new Category(-1);
            c.title = "SPLIT";
            return c;
        }
        return categoriesMap.get(id);
	}

    private Payee getPayee(long payeeId) {
        return payeeMap.get(payeeId);
    }

    private Project getProjectById(long projectId) {
        return projectMap.get(projectId);
    }

    private MyLocation getLocationById(long locationId) {
        return locationMap.get(locationId);
    }

}
