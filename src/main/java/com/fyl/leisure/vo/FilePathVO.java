package com.fyl.leisure.vo;

/**
 * @author Leisure
 * @version 1.0.0
 * @date 2024/4/28 14:17
 * @description
 */
public class FilePathVO {

    private String daoPath;
    private String dtoPath;

    private String voPath;

    public String getVoPath() {
        return this.voPath;
    }

    public void setVoPath(String voPath) {
        this.voPath = voPath;
    }

    public String getDaoPath() {
        return this.daoPath;
    }

    public void setDaoPath(String daoPath) {
        this.daoPath = daoPath;
    }

    public String getDtoPath() {
        return this.dtoPath;
    }

    public void setDtoPath(String dtoPath) {
        this.dtoPath = dtoPath;
    }
}
