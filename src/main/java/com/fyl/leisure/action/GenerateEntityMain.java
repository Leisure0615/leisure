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
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
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
import java.util.ArrayList;
import java.util.List;

/**
 * @author tangzhipeng
 * @project leisure
 * @description:
 * @date 4/26/2024 1:59 PM
 */
public class GenerateEntityMain extends AnAction {
    public GenerateEntityMain() {
        super("GenerateEntityMain");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        //获取当前项目
        Project project = e.getData(PlatformDataKeys.PROJECT);
        // 创建一个对话框
        DialogBuilder builder = new DialogBuilder(project);
        builder.setTitle("请输入MySQL数据库信息和作者信息");
        // 界面布局
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

        // 添加文本框和标签
        JTextField ipField = new JTextField();
        JTextField portField = new JTextField("3306");
        JTextField accountField = new JTextField();
        JTextField passwordField = new JTextField();
        JTextField databaseField = new JTextField();
        JTextField authorField = new JTextField();

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

        builder.setCenterPanel(panel);
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
                generateMethod(e.getProject().getName(), chosenFiles[0], ipField.getText(), portField.getText(), accountField.getText(), passwordField.getText(), databaseField.getText(), authorField.getText());
                // 成功生成代码，关闭对话框
                builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
                Messages.showInfoMessage("生成代码成功（代码已生成在文件中，若IDEA目录结构中没有显示，可以重启IDEA）", "运行成功");
                // 刷新侧边栏目录
                ApplicationManager.getApplication().runWriteAction(() -> {
                    ProjectView projectView = ProjectView.getInstance(project);
                    if (projectView != null) {
                        projectView.refresh();
                    }
                });
            } catch (Exception ex) {
                Messages.showErrorDialog("请检查数据库连接验证信息和数据库名是否输入正确", "生成实体类代码失败");
            }
        });
        builder.show();
    }

    /**
     * 代码生成方法
     *
     * @param projectName 项目名称
     * @param chosenFile  代码输出目录
     * @param ip          数据库地址
     * @param port        端口
     * @param account     用户
     * @param password    密码
     * @param database    数据库名
     * @param author      作者
     */
    public static void generateMethod(String projectName, VirtualFile chosenFile, String ip, String port, String account, String password, String database, String author) {
        AutoGenerator mpg = new AutoGenerator();
        TemplateConfig templateconfig = new TemplateConfig();
        templateconfig.setXml(null).setController(null);
        mpg.setTemplate(templateconfig);
        mpg.setPackageInfo(packageConfig(projectName));//全局配置
        mpg.setGlobalConfig(globalConfig(chosenFile, author));//数据源配
        mpg.setDataSource(dataSource(ip, port, account, password, database));
        // 策略配置
        mpg.setStrategy(strategy());
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
        gc.setOutputDir(chosenFile.getPath());    //生成文件存放的位位置
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
     * @return 策略
     */
    private static StrategyConfig strategy() {
        StrategyConfig strategy = new StrategyConfig();
        strategy.setLogicDeleteFieldName("delete_flag");
        strategy.setTablePrefix(""); // 去掉表名前缓
        strategy.setNaming(NamingStrategy.underline_to_camel);//表名生成策略(underline_to_camel,下划线转驼峰命名)
        strategy.setExclude("interface_info");
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setSuperControllerClass("alp.starcode.mcsu.framework.base.BaseController");//自定义继承的Controller类全
        strategy.setEntityLombokModel(true);
        strategy.setSuperMapperClass("com.github.yuichang.base.MPJBaseMapper");
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
