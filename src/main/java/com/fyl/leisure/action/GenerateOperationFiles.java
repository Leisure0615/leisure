package com.fyl.leisure.action;

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
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;


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
        if (data instanceof VirtualFile) {
            VirtualFile clickedFile = (VirtualFile) data;
            if (clickedFile.isValid()) {
                //文件路径 文件名称
                System.out.println("Clicked file path: " + clickedFile.getPath());
                System.out.println("Clicked file name: " + clickedFile.getName());
                try (FileInputStream fileInputStream = new FileInputStream(clickedFile.getPath())) {
                    ParseResult<CompilationUnit> result = new JavaParser().parse(fileInputStream);
                    if (result.isSuccessful()) {
                        CompilationUnit compilationUnit = result.getResult().orElse(null);
                        if (compilationUnit != null) {
                            ClassOrInterfaceDeclaration targetClass = compilationUnit.getClassByName(/*clickedFile.getName()*/"Article").orElse(null);
                            System.out.println("第四" + targetClass);
                            if (targetClass != null) {
                                for (FieldDeclaration field : targetClass.getFields()) {
                                    FiledVO filedVO = new FiledVO();
                                    filedVO.setFiledName(field.getVariables().get(0).getName().asString());
                                    filedVO.setFiledType(field.getVariables().get(0).getType().asString());
                                    filedVO.setFiledAnnotation(field.getAnnotations());
                                    filedVOS.add(filedVO);
                                }
                                System.out.println(filedVOS);
                            }
                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        //构目
        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        //获取选中的目
        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project, projectDir);
        if (virtualFile != null && virtualFile.isDirectory()) {
            System.out.println("Selected directory: " + virtualFile.getPath());
            //生成文件
            try {
                createController(virtualFile, filedVOS);
                createService(virtualFile);
                createModel(virtualFile);
                createController(virtualFile, filedVOS);

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            System.out.println("No directory selected");
        }
    }


    /**
     * 生成文件
     *
     * @param parentDirectory
     */
    private static void createFolders(VirtualFile parentDirectory) throws IOException {
//        createController(parentDirectory, filedVOS);
        createService(parentDirectory);
        createModel(parentDirectory);
    }

    private static void createController(VirtualFile parentDirectory, List<FiledVO> filedVOS) throws IOException {
        VirtualFile controller = parentDirectory.createChildDirectory(null, "controller");

        Application applicationManager = ApplicationManager.getApplication();
        VelocityEngine velocityEngine = new VelocityEngine();
        applicationManager.runWriteAction(() -> {
                    velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, controller.getPath());
//                    velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
                    velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());
                    velocityEngine.init();
                }
        );
        Template template = velocityEngine.getTemplate("template/DTO.vm");
        VelocityContext ctx = new VelocityContext();
        ctx.put("className", "Article");
        ctx.put("filedList", filedVOS);
        StringWriter sw = new StringWriter();
        template.merge(ctx, sw);
    }

    private static void createService(VirtualFile parentDirectory) throws IOException {
        VirtualFile service = parentDirectory.createChildDirectory(null, "service");
    }

    private static void createModel(VirtualFile parentDirectory) throws IOException {
        VirtualFile model = parentDirectory.createChildDirectory(null, "model");
        VirtualFile dto = model.createChildDirectory(null, "dto");
        VirtualFile vo = model.createChildDirectory(null, "vo");
    }
}
