package ru.orangesoftware.financisto2.model;

import javax.persistence.Column;

public class ActiveMyEntity extends MyEntity {

    @Column(name = "is_active")
    public boolean isActive;

}
