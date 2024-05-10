package com.fyl.leisure.dto;

import com.fyl.leisure.vo.FilePathVO;
import com.fyl.leisure.vo.FiledVO;
import com.intellij.openapi.vfs.VirtualFile;
import freemarker.template.Configuration;

import java.util.List;

/**
 * @author Leisure
 * @version 1.0.0
 * @date 2024/5/7 9:52
 * @description
 */
public class CreateFileDTO {

    /**
     * 实体类解析字段集合
     */
    private List<FiledVO> filedVOS;

    /**
     * 实体类名称
     */
    private String className;

    /**
     * 实体类描述
     */
    private String entityDescription;

    /**
     * 选择的实体类对象
     */
    private VirtualFile entityFile;

    /**
     * 右键选择的目录对象
     */
    private VirtualFile chooseDir;

    /**
     * Id字段名称
     */
    private String idField;

    /**
     * Id描述
     */
    private String idDescription;

    /**
     * 作者名称持
     */
    private String authorField;

    /**
     * Service路径名称
     */
    private String servicePath;

    /**
     * DTO与VO路径对象
     */
    private FilePathVO filePathVO;

    /**
     * 模板设置对象
     */
    private Configuration configuration;

    public String getServicePath() {
        return this.servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public FilePathVO getFilePathVO() {
        return this.filePathVO;
    }

    public void setFilePathVO(FilePathVO filePathVO) {
        this.filePathVO = filePathVO;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public List<FiledVO> getFiledVOS() {
        return this.filedVOS;
    }

    public void setFiledVOS(List<FiledVO> filedVOS) {
        this.filedVOS = filedVOS;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getEntityDescription() {
        return this.entityDescription;
    }

    public void setEntityDescription(String entityDescription) {
        this.entityDescription = entityDescription;
    }

    public VirtualFile getEntityFile() {
        return this.entityFile;
    }

    public void setEntityFile(VirtualFile entityFile) {
        this.entityFile = entityFile;
    }

    public VirtualFile getChooseDir() {
        return this.chooseDir;
    }

    public void setChooseDir(VirtualFile chooseDir) {
        this.chooseDir = chooseDir;
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

    public String getAuthorField() {
        return this.authorField;
    }

    public void setAuthorField(String authorField) {
        this.authorField = authorField;
    }
}
