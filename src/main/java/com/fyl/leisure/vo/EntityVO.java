package com.fyl.leisure.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Leisure
 * @version 1.0.0
 * @date 2024/5/6 11:34
 * @description
 */

public class EntityVO {
    private String entityDescription;

    private List<FiledVO> filedVOList;

    public String getEntityDescription() {
        return this.entityDescription;
    }

    public void setEntityDescription(String entityDescription) {
        this.entityDescription = entityDescription;
    }

    public List<FiledVO> getFiledVOList() {
        return this.filedVOList;
    }

    public void setFiledVOList(List<FiledVO> filedVOList) {
        this.filedVOList = filedVOList;
    }
}
