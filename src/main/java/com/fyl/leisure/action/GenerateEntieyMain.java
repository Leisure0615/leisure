package com.fyl.leisure.action;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.github.yulichang.base.MPJBaseService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangzhipeng
 * @project leisure
 * @description:
 * @date 4/26/2024 1:59 PM
 */
public class GenerateEntieyMain extends AnAction {
    public GenerateEntieyMain() {
        super("GenerateEntieyMain");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        //获取当前项目目录
        Project project = e.getData(PlatformDataKeys.PROJECT);
        VirtualFile projectDir = project.getBaseDir();
        if (projectDir == null) {
            return;
        }
        generateMethod();
    }

    static final String OUT_PATH = System.getProperty("user.dir");
    static final String MODULE_NAME = "alp-scaffolding framework";
    static final String DATABASES_PATH = "10.20.32.30:3306";
    //NOSONAR 仅特例，用于代码生成1个用法static final String DATABASES_NAME = "alp scaffolding db",2 个用法
    public static final String USER_NAME = "root";
    public static final String USER_PSW = "123456";
    //    public static final String DATABASE_URL = "jdbc:mySql://" + DATABASES_PATH + "/" + DATABASES_NAME + "?useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimeZone=UTC";
    public static final String DATABASE_URL = "jdbc:mysql://" + "localhost" + "3306" + "/" + "poetize" + "?useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimeZone=UTC";
    public static final String PARENT_PACKAGE = "alp.starcode.alpscaffolding.framework.database.mariadb.mybatis";

    public static void generateMethod() {
        AutoGenerator mpg = new AutoGenerator();
        TemplateConfig templateconfig = new TemplateConfig();
        templateconfig.setXml(null).setController(null);
        mpg.setTemplate(templateconfig);
        mpg.setPackageInfo(packagecanfig());//全局配置
        mpg.setGlobalConfig(globdLcenfig());//数据源配置
        mpg.setDataSource(datasource());
        // 策略配置
        mpg.setStrategy(strategy());
        // 执行生成
        mpg.execute();
    }

    private static DataSourceConfig dataSource() {
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setDbType(DbType.MYSQL);//mysol生成用DhType.MYSQL
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername(USER_NAME);
        dsc.setPassword(USER_PSW);
        dsc.setUrl(DATABASE_URL);
        return dsc;
    }

    private static PackageConfig packageConfig() {
        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent(PARENT_PACKAGE);//父包名
        pc.setService(SERVICE_PACKAGE_Service);
        pc.setServiceImpl(SERVICE_IMPL_PACKAGE); // Service In
        pc.setMapper(MAPPER_PACKAGE);    // Mapper包名
        pc.setEntity(ENTITY_PACKAGE);
        return pc;
    }

    private static GlobalConfig globalConfig() {
        GlobalConfig gc = new GlobalConfig();
        gc.setIdType(IdType.INPUT);
        gc.setOpen(false);
        gc.setSwagger2(SWAGGER_ENABLE);
//        gc.set0utputDir(OUT_PATH + "\\" + "MODULE_NAME" + "\\src\\main\\java");    //生成文件存放的位置
        gc.setOutputDir("\\src\\main\\java");    //生成文件存放的位置
        gc.setFileOverride(RECOVER_ENABLE);
        gc.setActiveRecord(false);
        gc.setEnableCache(false);
        gc.setBaseResultMap(true);
        gc.setBaseColumnList(true);
        gc.setAuthor("tangzhipeng");
        gc.setServiceName("%sDao");// service 命名方式
        gc.setServiceImplName("%sDaoImpl");// service impl争名方式
        gc.setMapperName("%sMapper");
        return gc;
    }

    private static StrategyConfig strategy() {
        StrategyConfig strategy = new StrategyConfig();
        strategy.setLogicDeleteFieldName("delete_fiag");
        strategy.setTablePrefix(""); // 去掉表名前缓
        strategy.setNaming(NamingStrategy.underline_to_camel);//表名生成策略(underline_to_camel,下划线转驼峰命名)
        strategy.setExclude("interface_info");
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setSuperControllerClass("nlo.ssarpode aase,framemark.base.BaseController");//自定义继承的Controller类全移
        strategy.setEntityLombokModel(true);
        strategy.setSuperMapperClass("com.github.yuichang.base.MPJBaseMapper");
        strategy.setSuperServiceClass(MPJBaseService.class);
        strategy.setSuperServiceImplClass(MPJBaseServiceImpl.class);
        strategy.setChainModel(true);
        List<TableFill> tableFills = new ArrayList<>();
        tableFills.add(new TableFill("tereate_user_id", FieldFill.INSERT));
        tableFills.add(new TableFill("create_user_name", FieldFill.INSERT));
        tableFills.add(new TableFill("create_time ", FieldFill.INSERT));
        tableFills.add(new TableFill("update_user_id", FieldFill.INSERT_UPDATE));
        tableFills.add(new TableFill("update_user_name", FieldFill.INSERT_UPDATE));
        tableFills.add(new TableFill("update_time", FieldFill.INSERT_UPDATE));
        strategy.setTableFillList(tableFills);
        return strategy;
    }

    //= -==== ==不需要修改配置======
    // 开启swagger2模式
    public static final Boolean SWAGGER_ENABLE = true;//是否覆盖已有文件1个用法
    public static final Boolean RECOVER_ENABLE = true;//dao包名1个用法
    public static final String SERVICE_PACKAGE = "dao";
    public static final String SERVICE_IMPL_PACKAGE = "dao.impl";//mapper包名1个用法
    public static final String MAPPER_PACKAGE = "mapper";//entity包名1个用法
    public static final String ENTITY_PACKAGE = "entity";

}
