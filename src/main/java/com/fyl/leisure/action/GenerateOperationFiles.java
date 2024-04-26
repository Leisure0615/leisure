package com.fyl.leisure.action;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
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
        Object data = e.getDataContext().getData("virtualFile");
        if (data instanceof VirtualFile) {
            VirtualFile clickedFile = (VirtualFile) data;
            if (clickedFile.isValid()) {
                //文件路径 文件名称
                System.out.println("Clicked file path: " + clickedFile.getPath());
                System.out.println("Clicked file name: " + clickedFile.getName());
                try (FileInputStream fileInputStream = new FileInputStream(clickedFile.getPath())) {
                    System.out.println("第一" + fileInputStream);
                    ParseResult<CompilationUnit> result = new JavaParser().parse(fileInputStream);
                    System.out.println("第二" + result);
                    if (result.isSuccessful()) {
                        CompilationUnit compilationUnit = result.getResult().orElse(null);
                        List<ClassOrInterfaceDeclaration> all = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
                        System.out.println("第三" + all);
                        if (compilationUnit != null) {
                            ClassOrInterfaceDeclaration targetClass = compilationUnit.getClassByName(/*clickedFile.getName()*/"Article").orElse(null);
                            System.out.println("第四" + targetClass);
                            if (targetClass != null) {
                                for (FieldDeclaration field : targetClass.getFields()) {
                                    String filedType = field.getVariables().get(0).getType().asString();
                                    String filedName = field.getVariables().get(0).getName().asString();
                                    NodeList<AnnotationExpr> annotations = field.getAnnotations();
                                    System.out.println(filedType);
                                    System.out.println(filedName);
                                    System.out.println(annotations);
                                }
                            }

                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        //生成文件
        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project, projectDir);
        if (virtualFile != null && virtualFile.isDirectory()) {
            System.out.println("Selected directory: " + virtualFile.getPath());

            createFolders(virtualFile, "Folder1", "Folder2", "Folder3");
        } else {
//            System.out.println("No directory selected");
        }
    }


    /**
     * 生成文件
     *
     * @param parentDirectory
     * @param folderNames
     */
    private static void createFolders(VirtualFile parentDirectory, String... folderNames) {

        for (String folderName : folderNames) {
            try {

                VirtualFile newFolder = parentDirectory.createChildDirectory(null, folderName);
                if (newFolder != null) {
                    System.out.println("Folder created: " + newFolder.getPath());
                } else {
                    System.out.println("Failed to create folder: " + folderName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}