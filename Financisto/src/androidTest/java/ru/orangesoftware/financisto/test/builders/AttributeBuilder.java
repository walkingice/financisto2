/*
 * Copyright (c) 2012 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package ru.orangesoftware.financisto.test.builders;

import ru.orangesoftware.financisto.db.DatabaseAdapter;
import ru.orangesoftware.financisto.db.MyEntityManager;
import ru.orangesoftware.financisto.model.Attribute;
import ru.orangesoftware.financisto.model.TransactionAttribute;

/**
 * Created by IntelliJ IDEA.
 * User: denis.solonenko
 * Date: 7/5/12 11:43 PM
 */
public class AttributeBuilder {

    private final MyEntityManager em;

    private AttributeBuilder(MyEntityManager em) {
        this.em = em;
    }

    public static AttributeBuilder withDb(MyEntityManager em) {
        return new AttributeBuilder(em);
    }

    public Attribute createTextAttribute(String name) {
        return createAttribute(name, Attribute.TYPE_TEXT);
    }

    public Attribute createNumberAttribute(String name) {
        return createAttribute(name, Attribute.TYPE_NUMBER);
    }

    private Attribute createAttribute(String name, int type) {
        Attribute a = new Attribute();
        a.name = name;
        a.type = type;
        a.id = em.saveOrUpdate(a);
        return a;
    }

    public static TransactionAttribute attributeValue(Attribute a, String value) {
        TransactionAttribute ta = new TransactionAttribute();
        ta.attributeId = a.id;
        ta.value = value;
        return ta;
    }

}
