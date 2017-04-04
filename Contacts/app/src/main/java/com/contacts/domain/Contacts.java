package com.contacts.domain;

/**
 * Created by c on 2017/3/15.
 */

public class Contacts {

    /**
     * 联系人姓名
     */
    private String name;

    /**
     * 联系电话
     */
    private String phone;


    /**
     * 排序字母
     */
    private String sortKey;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    @Override
    public String toString() {
        return "Contacts{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", sortKey='" + sortKey + '\'' +
                '}';
    }
}
