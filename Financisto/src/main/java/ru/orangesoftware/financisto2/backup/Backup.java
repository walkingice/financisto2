/*******************************************************************************
 * Copyright (c) 2010 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * <p/>
 * Contributors:
 * Denis Solonenko - initial API and implementation
 ******************************************************************************/
package ru.orangesoftware.financisto2.backup;

import android.content.Context;

import ru.orangesoftware.financisto2.export.Export;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

public final class Backup {

    public static final String[] BACKUP_TABLES = {
            "account",
            "attributes",
            "category_attribute",
            "transaction_attribute",
            "budget",
            "category",
            "currency",
            "project",
            "transactions",
            "payee",
            "ccard_closing_date",
            "currency_exchange_rate"
    };

    public static final String[] BACKUP_TABLES_WITH_SYSTEM_IDS = {
            "attributes", "category", "project"
    };

    private Backup() {
    }

    public static String[] listBackups(Context context) {
        File backupPath = Export.getBackupFolder(context);
        String[] files = backupPath.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".backup");
            }
        });
        if (files != null) {
            Arrays.sort(files, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s2.compareTo(s1);
                }
            });
            return files;
        } else {
            return new String[0];
        }
    }

    public static boolean tableHasSystemIds(String tableName) {
        for (String table : BACKUP_TABLES_WITH_SYSTEM_IDS) {
            if (table.equalsIgnoreCase(tableName)) {
                return true;
            }
        }
        return false;
    }

}
