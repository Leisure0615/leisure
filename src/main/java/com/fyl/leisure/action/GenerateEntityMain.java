package com.fyl.leisure.action;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.github.yulichang.base.MPJBaseService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.*;

/**
 * @author tangzhipeng
 * @project leisure
 * @description:
 * @date 4/26/2024 1:59 PM
 */
public class GenerateEntityMain extends AnAction {

    // 将下拉列表放在全局变量中以便后续使用
    private String[] excludeTableNames;

    // 在类的开头定义一个集合来存储选中的表名
    private Set<String> selectedTablesSet = new HashSet<>();

    // 在类的开头定义一个标签来显示选中的表名
    JLabel selectedTablesLabel = new JLabel();

    public GenerateEntityMain() {
        super("新建MybatisPlus Dao层代码");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        //获取当前项目
        Project project = e.getData(PlatformDataKeys.PROJECT);
        // 创建一个对话框
        DialogBuilder builder = new DialogBuilder(project);
        builder.setTitle("请输入MySQL数据库信息和作者信息");
        // 创建一个主面板，使用 BorderLayout 布局管理器
        JPanel mainPanel = new JPanel(new BorderLayout());
        // 界面布局
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        // 将第一个面板添加到主面板的中心（Center）
        mainPanel.add(panel, BorderLayout.CENTER);

        // 获取上次保存的值
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        String lastHost = propertiesComponent.getValue("lastHost", "");
        String lastPort = propertiesComponent.getValue("lastPort", "3306");
        String lastUser = propertiesComponent.getValue("lastUser", "");
        String lastPassword = propertiesComponent.getValue("lastPassword", "");
        String lastDatabase = propertiesComponent.getValue("lastDatabase", "");
        String lastAuthor = propertiesComponent.getValue("lastAuthor", "");
        // 添加文本框和标签
        JTextField ipField = new JTextField(lastHost);
        JTextField portField = new JTextField(lastPort);
        JTextField accountField = new JTextField(lastUser);
        JTextField passwordField = new JTextField(lastPassword);
        JTextField databaseField = new JTextField(lastDatabase);
        String userName = System.getProperty("user.name");
        JTextField authorField = new JTextField("".equals(lastAuthor) ? userName : lastAuthor);

        panel.add(new JLabel("Host:"));
        panel.add(ipField);
        panel.add(new JLabel("Port:"));
        panel.add(portField);
        panel.add(new JLabel("User:"));
        panel.add(accountField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Database:"));
        panel.add(databaseField);
        panel.add(new JLabel("Author:"));
        panel.add(authorField);
        if (selectedTablesLabel.getParent() != null) {
            panel.add(new JLabel("排除的表:"));
            panel.add(selectedTablesLabel);
        }
        // 添加连接数据库按钮
        JButton connectButton = new JButton("排除指定表");
        // 检查之前是否选中过该表，如果是，则设置为选中状态
        String[] tableNames = propertiesComponent.getValues("selectedTables");
        if (tableNames != null) {
            String name = String.join(",", tableNames);
            selectedTablesSet.addAll(Arrays.asList(tableNames));
            excludeTableNames = tableNames;
            selectedTablesLabel.setText(name);
        }
        // 如果标签还没有添加到面板上，就添加它
        if (selectedTablesLabel.getParent() == null && !(tableNames == null || tableNames.length == 0)) {
            panel.add(new JLabel("排除的表:"));
            panel.add(selectedTablesLabel);
        }
        // 创建第二个面板
        JPanel panel2 = new JPanel();

        // 修改原有按钮的监听器
        connectButton.addActionListener(actionEvent -> {
            // 获取用户输入的数据库连接信息
            String host = ipField.getText();
            String port = portField.getText();
            String user = accountField.getText();
            String password = passwordField.getText();
            String database = databaseField.getText();

            // 尝试连接数据库
            try {
                // 手动加载MySQL的JDBC驱动程序
                Class.forName("com.mysql.cj.jdbc.Driver");

                // 连接数据库
                Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);

                // 连接成功，查询数据库中的表名
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet tables = metaData.getTables(database, null, "%", new String[]{"TABLE"});

                // 创建一个新的界面来显示表格样式的选择界面
                JDialog dialog = new JDialog((JFrame) null, "选择排除的表", true);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                // 获取连接成功后查询到的表名列表
                DefaultListModel<JCheckBox> checkBoxModel = new DefaultListModel<>();
                // 填充表名列表
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    JCheckBox checkBox = new JCheckBox(tableName);
                    if (selectedTablesSet.contains(tableName)) {
                        checkBox.setSelected(true);
                    }
                    checkBoxModel.addElement(checkBox);
                }

                // 创建一个完成按钮
                JButton doneButton = new JButton("完成");
                doneButton.addActionListener(e1 -> {
                    // 清空之前选中的表集合
                    selectedTablesSet.clear();
                    // 获取用户选中的表名
                    for (int i = 0; i < checkBoxModel.size(); i++) {
                        JCheckBox checkBox = checkBoxModel.getElementAt(i);
                        if (checkBox.isSelected()) {
                            selectedTablesSet.add(checkBox.getText());
                        }
                    }
                    // 在原来的面板上添加Label，显示用户选择的表名
                    StringBuilder selectedTableNames = new StringBuilder();
                    for (String tableName : selectedTablesSet) {
                        selectedTableNames.append(tableName).append(",");
                    }
                    // 去除最后一个逗号
                    if (selectedTableNames.length() > 0) {
                        selectedTableNames.deleteCharAt(selectedTableNames.length() - 1);
                    }
                    // 设置选中的表名到选中的表名标签
                    selectedTablesLabel.setText(selectedTableNames.toString());
                    excludeTableNames = selectedTableNames.toString().split(",");
                    // 如果标签还没有添加到面板上，就添加它
                    if (selectedTablesLabel.getParent() == null) {
                        panel.add(new JLabel("排除的表:"));
                        panel.add(selectedTablesLabel);
                    }
                    // 刷新面板
                    panel.revalidate();
                    panel.repaint();
                    // 关闭表名选择对话框
                    dialog.dispose();
                });

                // 创建一个面板来容纳复选框
                JPanel checkBoxPanel = new JPanel(new GridLayout(0, 1));
                for (int i = 0; i < checkBoxModel.size(); i++) {
                    checkBoxPanel.add(checkBoxModel.getElementAt(i));
                }
                // 添加复选框面板和完成按钮到对话框中
                dialog.getContentPane().add(new JScrollPane(checkBoxPanel), BorderLayout.CENTER);
                dialog.getContentPane().add(doneButton, BorderLayout.SOUTH);
                
                // 获取屏幕尺寸
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                // 计算对话框宽度为屏幕宽度的一半
                int dialogWidth = screenSize.width / 4;
                int dialogHeight = screenSize.height / 2;
                // 设置对话框的宽度为计算出的宽度，高度为默认值
                dialog.setPreferredSize(new Dimension(dialogWidth, dialogHeight));
                // 设置对话框大小并显示
                dialog.pack();
                // 将对话框置于屏幕中央
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);

            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                // JDBC驱动程序加载失败
                JOptionPane.showMessageDialog(null, "JDBC驱动程序加载失败，请检查MySQL驱动程序是否正确配置！", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                // 连接失败，给出错误提示
                JOptionPane.showMessageDialog(null, "数据库连接失败，请检查输入的数据库连接信息！", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel2.add(connectButton, Component.CENTER_ALIGNMENT);
        // 将第二个面板添加到主面板的底部
        mainPanel.add(panel2, BorderLayout.SOUTH);

        builder.setCenterPanel(mainPanel);
        builder.addOkAction().setText("下一步");
        builder.setOkOperation(() -> {
            // 弹出文件目录选择器
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
            descriptor.setTitle("选择目录");
            descriptor.setDescription("选择要生成方法的src/main/java目录（选择到java文件层）");
            VirtualFile[] chosenFiles = FileChooser.chooseFiles(descriptor, project, null);
            // 如果用户取消选择目录，则不执行后续操作
            if (chosenFiles.length == 0) {
                return;
            }
            // 执行 generateMethod() 方法
            try {
                generateMethod(e.getProject().getName(), chosenFiles[0], ipField.getText(), portField.getText(), accountField.getText(), passwordField.getText(), databaseField.getText(), authorField.getText(), excludeTableNames);
                // 成功生成代码，保存输入的值
                propertiesComponent.setValue("lastHost", ipField.getText());
                propertiesComponent.setValue("lastPort", portField.getText());
                propertiesComponent.setValue("lastUser", accountField.getText());
                propertiesComponent.setValue("lastPassword", passwordField.getText());
                propertiesComponent.setValue("lastDatabase", databaseField.getText());
                propertiesComponent.setValue("lastAuthor", authorField.getText());
                String[] tablesArray = selectedTablesSet.toArray(new String[0]);
                propertiesComponent.setValues("selectedTables", tablesArray);
                // 成功生成代码，关闭对话框
                builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
                Messages.showInfoMessage("生成代码成功（代码已生成在文件中，若IDEA目录结构中没有显示，可以尝试右击文件夹再点击从磁盘重新加载或重启IDEA）", "运行成功");
                // 刷新侧边栏目录和重载磁盘
                ApplicationManager.getApplication().runWriteAction(() -> {
                    ProjectView projectView = ProjectView.getInstance(project);
                    if (projectView != null) {
                        projectView.refresh();
                    }
                });
                //重新载入文件
                VirtualFile chooseDir = e.getData(CommonDataKeys.VIRTUAL_FILE);
                chooseDir.refresh(false, true);
            } catch (Exception ex) {
                Messages.showErrorDialog("请检查数据库连接验证信息和数据库名是否输入正确", "生成实体类代码失败");
            }
        });
        builder.show();
    }

    /**
     * 解析模板获取author
     *
     * @param header 注释
     * @return author
     */
    private static String parseAuthorFromHeader(String header) {
        // 此处使用简单的字符串处理方法，你可以根据具体情况使用正则表达式等更复杂的方法来解析
        int startIndex = header.indexOf("@author") + 7;
        int endIndex = header.indexOf("\n", startIndex);
        return header.substring(startIndex, endIndex).trim();
    }

    /**
     * 代码生成方法
     *
     * @param projectName       项目名称
     * @param chosenFile        代码输出目录
     * @param ip                数据库地址
     * @param port              端口
     * @param account           用户
     * @param password          密码
     * @param database          数据库名
     * @param author            作者
     * @param excludeTableNames 排除的表
     */
    public static void generateMethod(String projectName, VirtualFile chosenFile, String ip, String port, String account, String password, String database, String author, String[] excludeTableNames) {
        AutoGenerator mpg = new AutoGenerator();
        TemplateConfig templateconfig = new TemplateConfig();
        templateconfig.setXml(null).setController(null);
        mpg.setTemplate(templateconfig);
        mpg.setPackageInfo(packageConfig(projectName));//全局配置
        mpg.setGlobalConfig(globalConfig(chosenFile, author));//数据源配
        mpg.setDataSource(dataSource(ip, port, account, password, database));
        // 策略配置
        mpg.setStrategy(strategy(excludeTableNames));
        //解决模板引擎初始化失败的问题
        final ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(GenerateEntityMain.class.getClassLoader());
        // 执行生成
        mpg.execute();
        Thread.currentThread().setContextClassLoader(oldContextClassLoader);
    }

    /**
     * 包名配置
     *
     * @param projectName 项目名称
     * @return 包配置信息
     */
    private static PackageConfig packageConfig(String projectName) {
        // 包配置
        PackageConfig pc = new PackageConfig();
        String name = projectName.replaceAll("-", "");
        pc.setParent("alp.starcode." + name + ".framework.database.mariadb.mybatis");
        pc.setService(SERVICE_PACKAGE);
        pc.setServiceImpl(SERVICE_IMPL_PACKAGE);
        pc.setMapper(MAPPER_PACKAGE);
        pc.setEntity(ENTITY_PACKAGE);
        return pc;
    }

    /**
     * 数据源配置
     *
     * @param ip       数据库地址
     * @param port     端口
     * @param account  用户
     * @param password 密码
     * @param database 数据库名
     * @return 数据源信息
     */
    private static DataSourceConfig dataSource(String ip, String port, String account, String password, String database) {
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setDbType(DbType.MYSQL);//mysol生成用DhType.MYSQL
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername(account);
        dsc.setPassword(password);
        dsc.setUrl("jdbc:mysql://" + ip + ":" + port + "/" + database + "?useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimeZone=UTC&allowPublicKeyRetrieval=true");
        return dsc;
    }

    /**
     * 全局配置
     *
     * @param chosenFile 代码输出目录
     * @param author     作者
     * @return 全局配置
     */
    private static GlobalConfig globalConfig(VirtualFile chosenFile, String author) {
        GlobalConfig gc = new GlobalConfig();
        gc.setIdType(IdType.INPUT);
        gc.setOpen(false);
        gc.setSwagger2(SWAGGER_ENABLE);
        gc.setOutputDir(chosenFile.getPath());    //生成文件存放的位置
        gc.setFileOverride(RECOVER_ENABLE);
        gc.setActiveRecord(false);
        gc.setEnableCache(false);
        gc.setBaseResultMap(true);
        gc.setBaseColumnList(true);
        gc.setAuthor(author);
        gc.setServiceName("%sDao");// service 命名方式
        gc.setServiceImplName("%sDaoImpl");// service impl命名方式
        gc.setMapperName("%sMapper");
        return gc;
    }

    /**
     * 配置策略
     *
     * @param excludeTableNames 排除不生成的表
     * @return 策略
     */
    private static StrategyConfig strategy(String[] excludeTableNames) {
        StrategyConfig strategy = new StrategyConfig();
        strategy.setLogicDeleteFieldName("delete_flag");
        strategy.setTablePrefix(""); // 去掉表名前缓
        strategy.setNaming(NamingStrategy.underline_to_camel);//表名生成策略(underline_to_camel,下划线转驼峰命名)
        strategy.setExclude(excludeTableNames);
//        strategy.setExclude("interface_info");
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setSuperControllerClass("alp.starcode.mcsu.framework.base.BaseController");//自定义继承的Controller类全
        strategy.setEntityLombokModel(true);
        strategy.setSuperMapperClass("com.github.yulichang.base.MPJBaseMapper");
        strategy.setSuperServiceClass(MPJBaseService.class);
        strategy.setSuperServiceImplClass(MPJBaseServiceImpl.class);
        strategy.setChainModel(true);//生成build代码
        List<TableFill> tableFills = new ArrayList<>();
        tableFills.add(new TableFill("create_user_id", FieldFill.INSERT));
        tableFills.add(new TableFill("create_user_name", FieldFill.INSERT));
        tableFills.add(new TableFill("create_time ", FieldFill.INSERT));
        tableFills.add(new TableFill("update_user_id", FieldFill.INSERT_UPDATE));
        tableFills.add(new TableFill("update_user_name", FieldFill.INSERT_UPDATE));
        tableFills.add(new TableFill("update_time", FieldFill.INSERT_UPDATE));
        strategy.setTableFillList(tableFills);
        return strategy;
    }

    //----------------------------固定配置------------------------------------------
    //开启swagger2模式
    public static final Boolean SWAGGER_ENABLE = true;

    //是否覆盖已有文件
    public static final Boolean RECOVER_ENABLE = true;

    //dao包名
    public static final String SERVICE_PACKAGE = "dao";

    //service.impl包名
    public static final String SERVICE_IMPL_PACKAGE = "dao.impl";

    //mapper包名
    public static final String MAPPER_PACKAGE = "mapper";

    //entity包名
    public static final String ENTITY_PACKAGE = "entity";
    //----------------------------------------------------------------------------
}
