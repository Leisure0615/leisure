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
    private String filedName;

    /**
     * 字段数据类型
     */
    private String filedType;

    /**
     * 字段注释
     */
    private NodeList<AnnotationExpr> filedAnnotation;

    public String getFiledName() {
        return this.filedName;
    }

    public void setFiledName(String filedName) {
        this.filedName = filedName;
    }

    public String getFiledType() {
        return this.filedType;
    }

    public void setFiledType(String filedType) {
        this.filedType = filedType;
    }

    public NodeList<AnnotationExpr> getFiledAnnotation() {
        return this.filedAnnotation;
    }

    public void setFiledAnnotation(NodeList<AnnotationExpr> filedAnnotation) {
        this.filedAnnotation = filedAnnotation;
    }
}
