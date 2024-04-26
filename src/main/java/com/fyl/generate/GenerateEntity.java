package com.fyl.generate;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.toolkit.AES;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.github.yulichang.base.MPJBaseService;
import com.github.yulichang.base.MPJBaseServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangzhipeng
 * @project leisure
 * @description:
 * @date 4/26/2024 11:41 AM
 */
public class GenerateEntity {
    static final String OUT_PATH = System.getProperty("user.dir");
    static final String MODULE_NAME = "alp-scaffolding framework";
    static final String DATABASES_PATH = "10.20.32.30:3306";
    //NOSONAR 仅特例，用于代码生成1个用法static final String DATABASES_NAME = "alp scaffolding db",2 个用法
    public static final String USER_NAME = "root";
    public static final String USER_PSW = "123456";
    //    public static final String DATABASE_URL = "jdbc:mySql://" + DATABASES_PATH + "/" + DATABASES_NAME + "?useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimeZone=UTC";
    public static final String DATABASE_URL = "jdbc:mySql://" + localhost + "3306" + "/" + "blog" + "?useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimeZone=UTC";
    public static final String PARENT_PACKAGE = "alp.starcode.alpscaffolding.framework.database.mariadb.mybatis";

    public static void main(String[] args) {
        AutoGenerator mpg = new AutoGenerator();
        TemplateConfig templateconfig = new Templateconfig();
        templateConfig.setXml(null).setController(null);
        mpj.setTemplate(templateConfig);
        mpg.setPackageInfo(packagecanfig());//全局配置
        mpg.setGlobalconfig(globdLcenfig());//数据源配置
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

    private static GLobalConfig globalConfig() {
        GlobalConfig gc = new GlobalConfig();
        gc.setIdType(IdType.INPUT);
        gc.setOpen(false);
        gc.setSwagger2(SWAGGER_ENABLED);
//        gc.set0utputDir(OUT_PATH + "\\" + "MODULE_NAME" + "\\src\\main\\java");    //生成文件存放的位置
        gc.set0utputDir("\\src\\main\\java");    //生成文件存放的位置
        gc.setFileOverride(RECOVER_ENABLE);
        gc.setActiveRecord(false);
        gc.setEnableCache(false);
        gc.setBaseResultMap(true);
        gc.setBaseColumnList(true);
        gc.setAuthor("tangzhipeng");
        gc.setServiceNane("%sDao");// service 命名方式
        gc.setServiceImplNane("%sDaoImpl");// service impl争名方式
        gc.setMapperName("%sMapper");
        return gc;
    }

    private static StrategyConfig strategy() {
        strategyConfig strategy = new StrategyConfig();
        strategy.setLogicDeleteFieldName("delete_fiag");
        strategy.setTablePrefix(""); // 去掉表名前缓
        strategy.setNaming(NamingStrategy.underline_to_comel);//表名生成策略(underline_to_camel,下划线转驼峰命名)
        strategy.setExclude("interface_info");
        strategy.setColumnNaming(NamingStrategy.underLine_to_camel);
        strategy.setSuperControllerclass("nlo.ssarpode aase,framemark.base.BaseController");//自定义继承的Controller类全移
        strategy.setEntityLombokModel(true);
        strategy.setSuperMapperClass("com.github.yuichang.base.MPJBaseMapper");
        strategy.setSuperServiceClass(MPJBaseService.class);
        strategy.setSuperServiceImplClass(MPJBaseServiceImpl.class);
        strategy.setChainModel(true);
        List<TableFill> tableFills = new ArrayList<>();
        tableFills.add(new TableFill("tereate_user_id", FieldFilL.INSERT));
        tableFills.add(new TableFill("create_user_name", FieldFilL.INSERT));
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
