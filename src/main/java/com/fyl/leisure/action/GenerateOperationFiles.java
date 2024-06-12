package com.fyl.leisure.action;


import com.fyl.leisure.dto.CreateFileDTO;
import com.fyl.leisure.vo.EntityVO;
import com.fyl.leisure.vo.FilePathVO;
import com.fyl.leisure.vo.FiledVO;

import com.github.javaparser.JavaParser;import com.fyl.leisure.vo.IdFieldAndDescription;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Leisure
 * @version 1.0.0
 * @date 2024/4/25 13:35
 * @description
 */
public class GenerateOperationFiles extends AnAction {

    public GenerateOperationFiles() {

        super("新建Controller、Service代码");
    }

    /******************************************             可修改参数              ****************************************/
    private final static String parentDirName = "mariadb";//实体类存放目录

    private final static String[] removeField = {"deleteTime", "createUserId", "createUserName", "createTime", "isDetected", "updateTime",
            "updateUserName", "updateUserId", "deleted", "serialVersionUID"};//不需要放进DTO、Vo的字段

    /*********************************************************************************************************************/

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        VirtualFile projectDir = project.getBaseDir();
        if (projectDir == null) {
            return;
        }
        CreateFileDTO createFileDTO = new CreateFileDTO();
        //获取鼠标右键点击的目录
        VirtualFile chooseDir = e.getData(CommonDataKeys.VIRTUAL_FILE);
        //构建文件选择器 默认打开mariadb目录下的entity目录
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        VirtualFile entity = findEntityDir(projectDir);
        descriptor.setRoots(entity);
        //获取选中的实体类文件
        VirtualFile entityFile = FileChooser.chooseFile(descriptor, null, project, null);
        //获取实体类名字，去掉.java后缀
        String className;
        int dotIndex = entityFile.getName().lastIndexOf('.');
        if (dotIndex != -1) {
            className = entityFile.getName().substring(0, dotIndex);
        } else {
            className = entityFile.getName();
        }
        //获取实体类字段信息以及实体类描述
        List<FiledVO> filedVOS = new ArrayList<>();
        EntityVO entityVO = getFiledsByEntity(entityFile, className, filedVOS);
        filedVOS = entityVO.getFiledVOList();
        String entityDescription = entityVO.getEntityDescription();
        //筛选出Id字段
        Optional<IdFieldAndDescription> optionalIdFieldAndDescription = getIdField(filedVOS);
        IdFieldAndDescription idFieldAndDescription = optionalIdFieldAndDescription.get();
        String idField = idFieldAndDescription.getIdField();
        String idDescription = idFieldAndDescription.getIdDescription();
        if (idDescription == null) {
            idDescription = idField;
        }
        //获取@author信息
        // 获取上次保存的值
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        String defaultAuthor = propertiesComponent.getValue("defaultAuthor", "");
        JTextField authorField = new JTextField("".equals(defaultAuthor) ? "" : defaultAuthor);
        //弹出输入框输入作者名字并持久化
        DialogBuilder builder = new DialogBuilder(project);
        builder.setTitle("请输入作者信息");
        JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
        panel.add(new JLabel("Author :"));
        panel.add(authorField);
        builder.setCenterPanel(panel);
        //放入创建文件所需
        createFileDTO.setFiledVOS(filedVOS);
        createFileDTO.setClassName(className);
        createFileDTO.setEntityDescription(entityDescription);
        createFileDTO.setEntityFile(entityFile);
        createFileDTO.setChooseDir(chooseDir);
        createFileDTO.setIdField(idField);
        createFileDTO.setIdDescription(idDescription);
        builder.setOkOperation(() -> {
            createFileDTO.setAuthorField(authorField.getText());
            propertiesComponent.setValue("defaultAuthor", createFileDTO.getAuthorField());
            builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
            //在当前鼠标右键选择的目录生成文件
            generateDir(createFileDTO);
        });
        builder.show();
        chooseDir.refresh(false, true);
    }

    private static Optional<IdFieldAndDescription> getIdField(List<FiledVO> filedVOS) {
        for (FiledVO filedVO : filedVOS) {
            Optional<AnnotationExpr> tableIdAnnotation = filedVO.getFieldAnnotation().stream()
                    .filter(annotationExpr -> annotationExpr.getName().asString().equals("TableId"))
                    .findFirst();

            if (tableIdAnnotation.isPresent() && tableIdAnnotation.get() instanceof NormalAnnotationExpr) {
                NormalAnnotationExpr tableIdExpr = (NormalAnnotationExpr) tableIdAnnotation.get();
                List<MemberValuePair> tableIdPairs = tableIdExpr.getPairs();

                Optional<String> fieldNameOptional = Optional.of(filedVO.getFieldName());

                Optional<String> apiModelPropertyValue = filedVO.getFieldAnnotation().stream()
                        .filter(annotationExpr -> annotationExpr.getName().asString().equals("ApiModelProperty"))
                        .findFirst()
                        .flatMap(annotationExpr -> {
                            if (annotationExpr instanceof NormalAnnotationExpr) {
                                NormalAnnotationExpr apiModelPropertyExpr = (NormalAnnotationExpr) annotationExpr;
                                return apiModelPropertyExpr.getPairs().stream()
                                        .filter(pair -> pair.getName().asString().equals("value"))
                                        .findFirst()
                                        .map(MemberValuePair::getValue)
                                        .map(value -> value.toString().replaceAll("\"", ""));
                            }
                            return Optional.empty();
                        });

                String idDescription = apiModelPropertyValue.orElse(null);
                return fieldNameOptional.map(fieldName -> new IdFieldAndDescription(fieldName, idDescription));
            }
        }

        return Optional.empty(); // 没有找到符合条件的字段
    }

    /**
     * 在当前鼠标右键选择的目录生成文件
     *
     * @param createFileDTO 创建文件传输对象
     */
    private void generateDir(CreateFileDTO createFileDTO) {
        Application applicationManager = ApplicationManager.getApplication();
        applicationManager.runWriteAction(() -> {
            try {
                Configuration configuration = new Configuration();
                configuration.setClassForTemplateLoading(getClass(), "/templates");
                configuration.setDefaultEncoding("UTF-8");
                createFileDTO.setConfiguration(configuration);
                //生成DTO与VO
                FilePathVO filePathVO = createModel(createFileDTO);
                createFileDTO.setFilePathVO(filePathVO);
                //生成Service
                String servicePath = createService(createFileDTO);
                createFileDTO.setServicePath(servicePath);
                //生成Controller
                createController(createFileDTO);

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    /**
     * 根据实体类获取字段信息
     *
     * @param entityFile 实体类文件
     * @param className  实体类名字
     * @param filedVOS   字段空列表
     * @return List<FiledVO> 实体类字段集合
     */
    private static EntityVO getFiledsByEntity(VirtualFile entityFile, String className, List<FiledVO> filedVOS) {
        EntityVO entityVO = new EntityVO();
        entityVO.setEntityDescription("");
        if (entityFile.isValid()) {
            try (FileInputStream fileInputStream = new FileInputStream(entityFile.getPath())) {
                ParseResult<CompilationUnit> result = new JavaParser().parse(fileInputStream);
                CompilationUnit compilationUnit = result.getResult().orElse(null);
                ClassOrInterfaceDeclaration targetClass = compilationUnit.getClassByName(className).orElse(null);
                if (targetClass != null) {
                    for (FieldDeclaration field : targetClass.getFields()) {
                        FiledVO filedVO = new FiledVO();
                        filedVO.setFieldName(field.getVariables().get(0).getName().asString());
                        filedVO.setFieldType(field.getVariables().get(0).getType().toString());
                        filedVO.setFieldAnnotation(field.getAnnotations());
                        filedVOS.add(filedVO);
                    }
                    NodeList<AnnotationExpr> annotations = targetClass.getAnnotations();
                    for (AnnotationExpr annotation : annotations) {
                        if (annotation.getNameAsString().equals("ApiModel")) {
                            // 处理 @ApiModel 注解
                            if (annotation.isNormalAnnotationExpr()) {
                                // 如果是普通注解，提取其中的 description 属性
                                NormalAnnotationExpr normalAnnotation = annotation.asNormalAnnotationExpr();
                                NodeList<MemberValuePair> pairs = normalAnnotation.getPairs();
                                for (MemberValuePair pair : pairs) {
                                    if (pair.getNameAsString().equals("description")) {
                                        entityVO.setEntityDescription(pair.getValue().toString().replace("\"", ""));
                                        break; // 找到 description 属性后退出循环
                                    }
                                }
                            }
                        }
                    }
                }
                //去掉不需要的字段
                Set<String> fieldsToRemove = new HashSet<>(Arrays.asList(removeField));
                filedVOS = filedVOS.stream()
                        .filter(filedVO -> !fieldsToRemove.contains(filedVO.getFieldName()))
                        .collect(Collectors.toList());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        entityVO.setFiledVOList(filedVOS);
        return entityVO;
    }

    /**
     * 生成controller文件夹夹以及controller文件
     *
     * @param createFileDTO 创建文件传输对象
     */
    private static void createController(CreateFileDTO createFileDTO) throws IOException, TemplateException {
        VirtualFile[] children = createFileDTO.getChooseDir().getChildren();
        VirtualFile controller =null;
        boolean isExit=false;
        for (VirtualFile child : children) {
            if("controller".equals(child.getName())){
                controller=child;
                isExit=true;
            }
        }
        if(!isExit){
            controller=createFileDTO.getChooseDir().createChildDirectory(null, "controller");
        }
        // 将类名字首字母转换为小写
        char firstCharLower = Character.toLowerCase(createFileDTO.getClassName().charAt(0));
        String remainingString = createFileDTO.getClassName().substring(1);
        // 组合新的字符串
        String classObject = firstCharLower + remainingString;
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("package", convertPathToPackageName(controller.getPath()));
        dataMap.put("entityImport", convertPathToPackageName(createFileDTO.getEntityFile().getPath()));
        dataMap.put("dtoImport", createFileDTO.getFilePathVO().getDtoPath());
        dataMap.put("voImport", createFileDTO.getFilePathVO().getVoPath());
        dataMap.put("serviceImport", createFileDTO.getServicePath());
        dataMap.put("className", createFileDTO.getClassName());
        dataMap.put("classObject", classObject);
        dataMap.put("date", currentTime());
        dataMap.put("idField", createFileDTO.getIdField());
        dataMap.put("author", createFileDTO.getAuthorField());
        dataMap.put("entityDescription", createFileDTO.getEntityDescription());
        dataMap.put("idDescription", createFileDTO.getIdDescription());
        //判断文件是否已存在
        for (VirtualFile child : controller.getChildren()) {
            if((createFileDTO.getClassName() + "Controller.java").equals(child.getName())){
                child.delete(child.getPath());
            }
        }
        // step4 加载模版文件
        Template template = createFileDTO.getConfiguration().getTemplate("Controller.ftl");
        // step5 生成数据
        File docFile = new File(controller.getPath() + "\\" + createFileDTO.getClassName() + "Controller.java");
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docFile), "UTF-8"));
        // step6 输出文件
        template.process(dataMap, out);
    }

    /**
     * 生成service文件夹以及Service文件
     *
     * @param createFileDTO 创建文件传输对象
     */
    private static String createService(CreateFileDTO createFileDTO) throws IOException, TemplateException {
        VirtualFile[] children = createFileDTO.getChooseDir().getChildren();
        VirtualFile service =null;
        boolean isExit=false;
        for (VirtualFile child : children) {
            if("service".equals(child.getName())){
                service=child;
                isExit=true;
            }
        }
        if(!isExit){
            service=createFileDTO.getChooseDir().createChildDirectory(null, "service");
        }
        // 将类名字首字母转换为小写
        char firstCharLower = Character.toLowerCase(createFileDTO.getClassName().charAt(0));
        String remainingString = createFileDTO.getClassName().substring(1);
        String classObject = firstCharLower + remainingString;
        //将Id字段首字母转化为大写
        char firstCharUpperCase = Character.toUpperCase(createFileDTO.getIdField().charAt(0));
        String remainingStringId = createFileDTO.getIdField().substring(1);
        String IdField = firstCharUpperCase + remainingStringId;
        //根据点击的实体类中找到Dao包的路径
        VirtualFile daoPathByEntity = findDaoPathByEntity(createFileDTO.getEntityFile(), createFileDTO.getClassName());
        // 组合新的字符串
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("package", convertPathToPackageName(service.getPath()));
        dataMap.put("entityImport", convertPathToPackageName(createFileDTO.getEntityFile().getPath()));
        dataMap.put("daoImport", convertPathToPackageName(daoPathByEntity.getPath()));
        dataMap.put("dtoImport", createFileDTO.getFilePathVO().getDtoPath());
        dataMap.put("voImport", createFileDTO.getFilePathVO().getVoPath());
        dataMap.put("className", createFileDTO.getClassName());
        dataMap.put("classObject", classObject);
        dataMap.put("date", currentTime());
        dataMap.put("idField", createFileDTO.getIdField());
        dataMap.put("IdField", IdField);
        dataMap.put("author", createFileDTO.getAuthorField());
        //判断文件是否已存在
        for (VirtualFile child : service.getChildren()) {
            if((createFileDTO.getClassName() + "Service.java").equals(child.getName())){
                child.delete(child.getPath());
            }
        }
        // step4 加载模版文件
        Template template = createFileDTO.getConfiguration().getTemplate("Service.ftl");
        // step5 生成数据
        File docFile = new File(service.getPath() + "\\" + createFileDTO.getClassName() + "Service.java");
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docFile), "UTF-8"));
        // step6 输出文件
        template.process(dataMap, out);
        //返回Service文件路径
        return convertPathToPackageName(docFile.toString());
    }

    /**
     * 生成model文件夹
     *
     * @param createFileDTO 创建文件传输对象
     * @return FilePathVO DTO与VO路径对象
     */
    private static FilePathVO createModel(CreateFileDTO createFileDTO) throws Exception {
        VirtualFile model = null;
        boolean isExit=false;
        VirtualFile[] children = createFileDTO.getChooseDir().getChildren();
        for (VirtualFile child : children) {
            if ("model".equals(child.getName())) {
                model = child;
                isExit=true;
            }
        }
        if(!isExit){
            model = createFileDTO.getChooseDir().createChildDirectory(null, "model");
        }
        FilePathVO filePathVO = new FilePathVO();
        filePathVO.setDtoPath(creatDTO(createFileDTO, model));
        filePathVO.setVoPath(creatVO(createFileDTO, model));
        return filePathVO;
    }

    /**
     * 生成VO文件
     *
     * @param createFileDTO 创建文件传输对象
     * @param model         model目录对象
     */
    private static String creatVO(CreateFileDTO createFileDTO, VirtualFile model) throws Exception {
        VirtualFile[] children = model.getChildren();
        VirtualFile vo =null;
        boolean isExit=false;
        for (VirtualFile child : children) {
            if ("vo".equals(child.getName())) {
                vo=child;
                isExit=true;
            }
        }
        if(!isExit){
            vo = model.createChildDirectory(null, "vo");
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("package", convertPathToPackageName(vo.getPath()));
        dataMap.put("className", createFileDTO.getClassName());
        dataMap.put("fieldList", createFileDTO.getFiledVOS());
        dataMap.put("date", currentTime());
        dataMap.put("author", createFileDTO.getAuthorField());
        //判断文件是否已存在
        for (VirtualFile child : vo.getChildren()) {
            if((createFileDTO.getClassName() + "Vo.java").equals(child.getName())){
                child.delete(child.getPath());
            }
        }
        // step4 加载模版文件
        Template template = createFileDTO.getConfiguration().getTemplate("VO.ftl");
        // step5 生成数据
        File docFile = new File(vo.getPath() + "\\" + createFileDTO.getClassName() + "Vo.java");
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docFile), "UTF-8"));
        // step6 输出文件
        template.process(dataMap, out);
        //返回VO文件路径
        return convertPathToPackageName(docFile.toString());
    }

    /**
     * 生成DTO文件
     *
     * @param createFileDTO 创建文件传输对象
     * @param model         model目录对象
     */
    private static String creatDTO(CreateFileDTO createFileDTO, VirtualFile model) throws Exception {
        VirtualFile[] children = model.getChildren();
        VirtualFile dto =null;
        boolean isExit=false;
        for (VirtualFile child : children) {
            if ("dto".equals(child.getName())) {
                dto=child;
                isExit=true;
            }
        }
        if(!isExit){
            dto = model.createChildDirectory(null, "dto");
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("package", convertPathToPackageName(dto.getPath()));
        dataMap.put("className", createFileDTO.getClassName());
        dataMap.put("fieldList", createFileDTO.getFiledVOS());
        dataMap.put("date", currentTime());
        dataMap.put("author", createFileDTO.getAuthorField());
        //判断文件是否已存在
        for (VirtualFile child : dto.getChildren()) {
            if((createFileDTO.getClassName() + "Dto.java").equals(child.getName())){
                child.delete(child.getPath());
            }
        }
        // step4 加载模版文件
        Template template = createFileDTO.getConfiguration().getTemplate("DTO.ftl");
        // step5 生成数据
        File docFile = new File(dto.getPath() + "\\" + createFileDTO.getClassName() + "Dto.java");
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docFile), "UTF-8"));
        // step6 输出文件
        template.process(dataMap, out);
        //返回DTO文件路径
        return convertPathToPackageName(docFile.toString());
    }

    /**
     * 获取当前时间
     */
    private static String currentTime() {
        LocalDate currentDate = LocalDate.now();
        // 创建一个日期时间格式化器，用于格式化日期为"yyyy-MM-dd"的格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        // 格式化当前日期为字符串
        return currentDate.format(formatter);
    }

    /**
     * 将路径转化为import
     *
     * @param path
     * @return
     */
    public static String convertPathToPackageName(String path) {
        // 截取从 "java" 后面的部分，同时替换 "/" 为 "."
        int javaIndex = path.indexOf("java");
        if (javaIndex != -1 && javaIndex + 4 < path.length()) {
            String packagePath = path.substring(javaIndex + 5); // "+5" to skip "java/"
            String replacedPath = packagePath.replaceAll("[\\\\/]", ".");
            if (replacedPath.endsWith(".java")) {
                replacedPath = replacedPath.substring(0, replacedPath.length() - 5);
            }
            return replacedPath;
        } else {
            // 如果未找到 "java" 或路径格式不正确，返回空字符串或抛出异常
            return ""; // 或者抛出异常，视情况而定
        }
    }

    public static VirtualFile findDaoPathByEntity(VirtualFile clickedFile, String finalClassName) {
        String daoName = finalClassName + "Dao.java";
        // 假设你已经有了一个parentOfParent的VirtualFile对象
        VirtualFile parentOfParent = clickedFile.getParent().getParent(); // 你的上级目录的VirtualFile对象
        if (parentOfParent.getChildren() != null) {
            for (VirtualFile child : parentOfParent.getChildren()) {
                if (child.isDirectory() && "dao".equals(child.getName())) {
                    for (VirtualFile childChild : child.getChildren()) {
                        if (!childChild.isDirectory() && daoName.equals(childChild.getName())) {
                            return childChild;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 查询parentDirName包下的实体类路径
     */
    public static VirtualFile findEntityDir(VirtualFile projectDir) {
        return findEntityDirRecursive(projectDir, parentDirName, "entity");
    }

    /**
     * 查找mariadb目录下的entity目录
     */
    private static VirtualFile findEntityDirRecursive(VirtualFile currentDir, String parentDirName, String targetDirName) {
        // 检查当前目录是否是目标目录
        if (currentDir != null && currentDir.isDirectory() && currentDir.getName().equals(targetDirName)) {
            // 检查父目录是否是指定的父目录或其父级
            if (isParentDirectory(currentDir, parentDirName)) {
                return currentDir; // 找到符合条件的目标目录，立即返回
            }
        }
        // 递归查找子目录
        VirtualFile[] children = currentDir.getChildren();
        for (VirtualFile child : children) {
            VirtualFile found = findEntityDirRecursive(child, parentDirName, targetDirName);
            if (found != null) {
                return found; // 如果找到目标目录，立即返回
            }
        }
        return null; // 未找到目标目录，返回 null
    }

    // 检查当前目录的父目录是否是指定的父目录或其父级
    private static boolean isParentDirectory(VirtualFile currentDir, String parentDirName) {
        VirtualFile parentDir = currentDir.getParent();
        while (parentDir != null) {
            if (parentDir.getName().equals(parentDirName)) {
                return true; // 找到指定的父目录
            }
            parentDir = parentDir.getParent(); // 继续向上查找父目录
        }
        return false; // 没有找到指定的父目录
    }
}
