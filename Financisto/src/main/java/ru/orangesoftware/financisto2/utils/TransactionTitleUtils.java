package ru.orangesoftware.financisto2.utils;

import ru.orangesoftware.financisto2.model.Category;

import static ru.orangesoftware.financisto2.model.Category.isSplit;
import static ru.orangesoftware.financisto2.utils.Utils.isNotEmpty;

/**
 * Created by IntelliJ IDEA.
 * User: Denis Solonenko
 * Date: 7/19/11 7:53 PM
 */
public class TransactionTitleUtils {

    public static String generateTransactionTitle(StringBuilder sb, String payee, String note, long categoryId, String category) {
        if (isSplit(categoryId)) {
            return generateTransactionTitleForSplit(sb, payee, note, category);
        } else {
            return generateTransactionTitleForRegular(sb, payee, note, category);
        }
    }

    private static String generateTransactionTitleForRegular(StringBuilder sb, String payee, String note, String category) {
        String secondPart = joinAdditionalFields(sb, payee, note);
        if (isNotEmpty(category)) {
            if (isNotEmpty(secondPart)) {
                sb.append(category).append(" (").append(secondPart).append(")");
                return sb.toString();
            } else {
                return category;
            }
        } else {
            return secondPart;
        }
    }

    private static String joinAdditionalFields(StringBuilder sb, String payee, String note) {
        sb.setLength(0);
        append(sb, payee);
        append(sb, note);
        String secondPart = sb.toString();
        sb.setLength(0);
        return secondPart;
    }

    private static String generateTransactionTitleForSplit(StringBuilder sb, String payee, String note, String category) {
        String secondPart = joinAdditionalFields(sb, note);
        if (isNotEmpty(payee)) {
            if (isNotEmpty(secondPart)) {
                return sb.append("[").append(payee).append("...] ").append(secondPart).toString();
            }
            return sb.append("[").append(payee).append("...]").toString();
        } else {
            if (isNotEmpty(secondPart)) {
                return sb.append("[...] ").append(secondPart).toString();
            }
            return category;
        }
    }

    private static String joinAdditionalFields(StringBuilder sb, String note) {
        sb.setLength(0);
        append(sb, note);
        String secondPart = sb.toString();
        sb.setLength(0);
        return secondPart;
    }


    private static void append(StringBuilder sb, String s) {
        if (isNotEmpty(s)) {
            if (sb.length() > 0) {
                sb.append(": ");
            }
            sb.append(s);
        }
    }

}
