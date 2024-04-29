package com.fyl.leisure.vo;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AnnotationExpr;

/**
 * @author Leisure
 * @version 1.0.0
 * @date 2024/4/26 11:31
 * @description
 */
public class FiledVO {

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段数据类型
     */
    private String fieldType;

    /**
     * 字段注释
     */
    private NodeList<AnnotationExpr> fieldAnnotation;

    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return this.fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public NodeList<AnnotationExpr> getFieldAnnotation() {
        return this.fieldAnnotation;
    }

    public void setFieldAnnotation(NodeList<AnnotationExpr> fieldAnnotation) {
        this.fieldAnnotation = fieldAnnotation;
    }
}
