/*
 * Copyright (c) 2012 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package ru.orangesoftware.financisto2.utils;

import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.db.MyEntityManager;
import ru.orangesoftware.financisto2.model.Account;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: denis.solonenko
 * Date: 8/16/12 7:55 PM
 */
public class IntegrityCheck {

    private final DatabaseAdapter db;
    private final MyEntityManager em;

    public IntegrityCheck(DatabaseAdapter db, MyEntityManager em) {
        this.db = db;
        this.em = em;
    }

    public boolean isBroken() {
        return isRunningBalanceBroken();
    }

    private boolean isRunningBalanceBroken() {
        List<Account> accounts = em.getAllAccountsList();
        for (Account account : accounts) {
            long totalFromAccount = account.totalAmount;
            long totalFromRunningBalance = db.getLastRunningBalanceForAccount(account);
            if (totalFromAccount != totalFromRunningBalance) {
                return true;
            }
        }
        return false;
    }

}
