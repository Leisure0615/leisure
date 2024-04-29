package com.fyl.leisure.action;


import com.fyl.leisure.vo.FilePathVO;
import com.fyl.leisure.vo.FiledVO;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

        super("GenerateOperationFiles");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        //获取当前项目目录
        Project project = e.getData(PlatformDataKeys.PROJECT);
        VirtualFile projectDir = project.getBaseDir();
        if (projectDir == null) {
            return;
        }
        List<FiledVO> filedVOS = new ArrayList<>();
        Object data = e.getDataContext().getData("virtualFile");
        //获取实体类名字，去掉.java后缀
        String className = null;
        VirtualFile clickedFile;
        if (data instanceof VirtualFile) {
            clickedFile = (VirtualFile) data;
            // 使用lastIndexOf找到最后一个"."的位置
            int dotIndex = clickedFile.getName().lastIndexOf('.');
            // 使用substring获取没有扩展名的文件名
            if (dotIndex != -1) {
                className = clickedFile.getName().substring(0, dotIndex);
            } else {
                // 如果没有找到"."，说明文件名没有扩展名
                className = clickedFile.getName();
            }
            //获取实体类字段信息
            if (clickedFile.isValid()) {
                try (FileInputStream fileInputStream = new FileInputStream(clickedFile.getPath())) {
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
                    }
                    //去掉不需要的字段
                    Set<String> fieldsToRemove = new HashSet<>(Arrays.asList("deleteTime", "createUserId", "createUserName", "createTime",
                            "updateTime", "updateUserName", "updateUserId"));
                    filedVOS = filedVOS.stream()
                            .filter(filedVO -> !fieldsToRemove.contains(filedVO.getFieldName()))
                            .collect(Collectors.toList());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } else {
            clickedFile = null;
        }
        //构建目录
        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        //获取选中的目录
        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project, projectDir);
        if (virtualFile != null && virtualFile.isDirectory()) {
            //生成文件
            Application applicationManager = ApplicationManager.getApplication();
            List<FiledVO> finalFiledVOS = filedVOS;
            String finalClassName = className;
            applicationManager.runWriteAction(() -> {
                try {
                    Configuration configuration = new Configuration();
                    configuration.setClassForTemplateLoading(getClass(), "/templates");
                    Writer out = null;
                    //生成DTO与VO
                    FilePathVO filePathVO = createModel(virtualFile, finalFiledVOS, finalClassName, configuration, out);
                    //生成Service
                    String servicePath = createService(virtualFile, finalClassName, configuration, out, filePathVO, clickedFile);
                    //生成Controller
                    createController(virtualFile, finalClassName, configuration, out, filePathVO, clickedFile,servicePath);

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        } else {
            System.out.println("没找到该目录");
        }
    }

    /**
     * 生成controller文件夹夹以及controller文件
     *
     * @param parentDirectory
     * @param finalClassName
     * @param filePathVO
     * @param clickedFile
     * @param servicePath
     * @throws IOException
     * @throws TemplateException
     */
    private static void createController(VirtualFile parentDirectory, String finalClassName, Configuration configuration, Writer out, FilePathVO filePathVO, VirtualFile clickedFile, String servicePath) throws IOException, TemplateException {
        VirtualFile controller = parentDirectory.createChildDirectory(null, "controller");
        // 将类名字首字母转换为小写
        char firstCharLower = Character.toLowerCase(finalClassName.charAt(0));
        String remainingString = finalClassName.substring(1);
        //根据点击的实体类中找到Dao包的路径
        VirtualFile daoPathByEntity = findDaoPathByEntity(clickedFile, finalClassName);
        // 组合新的字符串
        String classObject = firstCharLower + remainingString;
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("package", convertPathToPackageName(controller.getPath()));
        dataMap.put("entityImport", convertPathToPackageName(clickedFile.getPath()));
        dataMap.put("dtoImport", filePathVO.getDtoPath());
        dataMap.put("voImport", filePathVO.getVoPath());
        dataMap.put("serviceImport", servicePath);
        dataMap.put("className", finalClassName);
        dataMap.put("classObject", classObject);
        dataMap.put("date", currentTime());
        // step4 加载模版文件
        Template template = configuration.getTemplate("Controller.ftl");
        // step5 生成数据
        File docFile = new File(controller.getPath() + "\\" + finalClassName + "Controller.java");
        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docFile)));
        // step6 输出文件
        template.process(dataMap, out);
    }

    /**
     * 生成service文件夹以及Service文件
     *
     * @param parentDirectory 选择的目录
     * @param finalClassName  实体类名字
     * @param filePathVO      路径对象，包括dto路径，dao路径，vo路径
     * @param clickedFile     实体类路径对象
     * @param configuration   模板设置对象
     * @return
     * @throws IOException
     */
    private static String createService(VirtualFile parentDirectory, String finalClassName, Configuration configuration, Writer out, FilePathVO filePathVO, VirtualFile clickedFile) throws IOException, TemplateException {
        VirtualFile service = parentDirectory.createChildDirectory(null, "service");
        // 将类名字首字母转换为小写
        char firstCharLower = Character.toLowerCase(finalClassName.charAt(0));
        String remainingString = finalClassName.substring(1);
        //根据点击的实体类中找到Dao包的路径
        VirtualFile daoPathByEntity = findDaoPathByEntity(clickedFile, finalClassName);
        // 组合新的字符串
        String classObject = firstCharLower + remainingString;
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("package", convertPathToPackageName(service.getPath()));
        dataMap.put("entityImport", convertPathToPackageName(clickedFile.getPath()));
        dataMap.put("daoImport", convertPathToPackageName(daoPathByEntity.getPath()));
        dataMap.put("dtoImport", filePathVO.getDtoPath());
        dataMap.put("voImport", filePathVO.getVoPath());
        dataMap.put("className", finalClassName);
        dataMap.put("classObject", classObject);
        dataMap.put("date", currentTime());
        // step4 加载模版文件
        Template template = configuration.getTemplate("Service.ftl");
        // step5 生成数据
        File docFile = new File(service.getPath() + "\\" + finalClassName + "Service.java");
        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docFile)));
        // step6 输出文件
        template.process(dataMap, out);
        //返回Service文件路径
        return convertPathToPackageName(docFile.toString());
    }

    /**
     * 生成model文件夹
     *
     * @param parentDirectory 选择目录
     * @param filedVOS        字段集合
     * @param finalClassName  实体类名字
     * @param configuration   模板设置对象
     * @throws IOException
     * @throws TemplateException
     */
    private static FilePathVO createModel(VirtualFile parentDirectory, List<FiledVO> filedVOS, String finalClassName, Configuration configuration, Writer out) throws IOException, TemplateException {
        VirtualFile model = parentDirectory.createChildDirectory(null, "model");
        FilePathVO filePathVO = new FilePathVO();
        filePathVO.setDtoPath(creatDTO(filedVOS, model, finalClassName, configuration, out));
        filePathVO.setVoPath(creatVO(filedVOS, model, finalClassName, configuration, out));
        return filePathVO;
    }

    /**
     * 生成VO文件
     *
     * @param filedVOS       字段集合
     * @param model          model目录对象
     * @param finalClassName 实体类名字
     * @param configuration  模板设置对象
     * @throws IOException
     * @throws TemplateException
     */
    private static String creatVO(List<FiledVO> filedVOS, VirtualFile model, String finalClassName, Configuration configuration, Writer out) throws IOException, TemplateException {
        VirtualFile vo = model.createChildDirectory(null, "vo");
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("package", convertPathToPackageName(vo.getPath()));
        dataMap.put("className", finalClassName);
        dataMap.put("fieldList", filedVOS);
        dataMap.put("date", currentTime());
        // step4 加载模版文件
        Template template = configuration.getTemplate("VO.ftl");
        // step5 生成数据
        File docFile = new File(vo.getPath() + "\\" + finalClassName + "VO.java");
        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docFile)));
        // step6 输出文件
        template.process(dataMap, out);
        System.out.println("VO路径   " + docFile);
        //返回VO文件路径
        return convertPathToPackageName(docFile.toString());
    }

    /**
     * 生成DTO文件
     *
     * @param filedVOS       字段集合
     * @param model          model目录对象
     * @param finalClassName 实体类名字
     * @param configuration  模板设置对象
     * @throws IOException
     * @throws TemplateException
     */
    private static String creatDTO(List<FiledVO> filedVOS, VirtualFile model, String finalClassName, Configuration configuration, Writer out) throws IOException, TemplateException {
        VirtualFile dto = model.createChildDirectory(null, "dto");
        Map<String, Object> dataMap = new HashMap<String, Object>();
        System.out.println(dto.getPath());
        dataMap.put("package", convertPathToPackageName(dto.getPath()));
        dataMap.put("className", finalClassName);
        dataMap.put("fieldList", filedVOS);
        dataMap.put("date", currentTime());
        // step4 加载模版文件
        Template template = configuration.getTemplate("DTO.ftl");
        // step5 生成数据
        File docFile = new File(dto.getPath() + "\\" + finalClassName + "DTO.java");
        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docFile)));
        // step6 输出文件
        template.process(dataMap, out);
        //返回DTO文件路径
        return convertPathToPackageName(docFile.toString());
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    private static String currentTime() {
        LocalDate currentDate = LocalDate.now();
        // 创建一个日期时间格式化器，用于格式化日期为"yyyy-MM-dd"的格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        // 格式化当前日期为字符串
        String formattedDate = currentDate.format(formatter);
        return formattedDate;
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
                        System.out.println(childChild.getName());
                        if (!childChild.isDirectory() && daoName.equals(childChild.getName())) {
                            return childChild;
                        }
                    }
                }
            }
        }
        return null;
    }
}
