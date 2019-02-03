package com.bookstore.books.model;


import java.util.Date;

public class TimeStamp {

    private Date lastCreatedAt;
    private Date lastUpdatedAt;

    public Date getLastCreatedAt() {
        return lastCreatedAt;
    }

    public void setLastCreatedAt(Date lastCreatedAt) {
        this.lastCreatedAt = lastCreatedAt;
    }

    public Date getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Date lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
