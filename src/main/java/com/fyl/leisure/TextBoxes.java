package com.fyl.leisure;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.intellij.icons.AllIcons.RunConfigurations.Application;


/**
 * @author Leisure
 * @version 1.0.0
 * @date 2024/4/25 13:35
 * @description
 */
public class TextBoxes extends AnAction {


    public TextBoxes() {

        super("GenerateTable");
        }
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        //获取当前项目目录
        Project project = e.getData(PlatformDataKeys.PROJECT);
        VirtualFile projectDir = project.getBaseDir();
        if(projectDir==null){
            return;
        }
        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false , false, false, false);
        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project, projectDir);
        if (virtualFile != null && virtualFile.isDirectory()) {
            System.out.println("Selected directory: " + virtualFile.getPath());

            createFolders(virtualFile, "Folder1", "Folder2", "Folder3");
        } else {
            System.out.println("No directory selected");
        }
    }

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
