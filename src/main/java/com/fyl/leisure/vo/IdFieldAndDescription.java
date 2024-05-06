package com.fyl.leisure.vo;

/**
 * @author Leisure
 * @version 1.0.0
 * @date 2024/5/6 13:34
 * @description
 */
public class IdFieldAndDescription {

    private String idField;

    private String idDescription;

    public IdFieldAndDescription(String idField, String idDescription) {
        this.idField = idField;
        this.idDescription = idDescription;
    }

    public String getIdField() {
        return this.idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }

    public String getIdDescription() {
        return this.idDescription;
    }

    public void setIdDescription(String idDescription) {
        this.idDescription = idDescription;
    }
}
