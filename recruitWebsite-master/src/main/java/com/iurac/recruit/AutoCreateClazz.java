package com.iurac.recruit;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import javax.sql.DataSource;

/**
 * AutoCreateClazz 类是一个使用 MyBatis Plus 的 AutoGenerator 类来自动生成代码的示例。
 * 这种自动生成代码的方式可以帮助开发者快速生成数据库表对应的实体类、Mapper 接口、Service 接口和实现类等代码，提高开发效率。
 *
 * 这段Java代码使用了MyBatis Plus的代码自动生成器（AutoGenerator），用于自动生成Mapper、Service和ServiceImpl等类。以下是这段代码的详细解释：
 *
 * 首先，创建了一个AutoGenerator对象。
 *
 * 然后，创建了一个GlobalConfig对象，并设置了一些全局配置。例如，设置了生成代码的输出目录、作者名称、Mapper、Service和ServiceImpl的命名规则，以及主键的类型。
 *
 * 接着，创建了一个DataSourceConfig对象，并设置了数据源的驱动名、用户名、密码和URL。
 *
 * 然后，创建了一个PackageConfig对象，并设置了包的父路径和模块名。
 *
 * 接着，创建了一个StrategyConfig对象，并设置了表前缀和命名策略。
 *
 * 最后，将这些配置对象设置到AutoGenerator对象中，并调用execute方法来执行代码生成。
 *
 * 所以，这段代码的效果是根据指定的数据源和配置，自动生成对应的Mapper、Service和ServiceImpl类。生成的代码将被输出到项目的src/main/java目录下，包路径为com.iurac.recruit，并且所有的表名和列名都将按照下划线转驼峰的规则进行命名。
 * */
public class AutoCreateClazz {

    public static void main(String[] args) {
        //创建自动生成器
        AutoGenerator autoGenerator = new AutoGenerator();

        //设置全局配置：
        //setOutputDir：设置生成的代码的输出目录为项目的 src/main/java 目录。
        //setAuthor：设置代码生成时的作者名称。
        //setMapperName、setServiceName、setServiceImplName：设置生成的 Mapper、Service 和 ServiceImpl 的命名规则。
        //setIdType：设置主键的类型为输入型（IdType.INPUT）。
        GlobalConfig globalConfig = new GlobalConfig();
        //设置生成路径
        String path = System.getProperty("user.dir");
        globalConfig.setOutputDir(path+"/src/main/java");
        globalConfig.setAuthor("zzj");
        //设置命名规则
        globalConfig.setMapperName("%sMapper");
        globalConfig.setServiceName("%sService");
        globalConfig.setServiceImplName("%sServiceImpl");
        //设置主键类型
        globalConfig.setIdType(IdType.INPUT);

        //设置数据源
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDriverName("com.mysql.cj.jdbc.Driver");
        dataSourceConfig.setUsername("root");
        dataSourceConfig.setPassword("12345678");
        dataSourceConfig.setUrl("jdbc:mysql://localhost:3306/recruitment?serverTimezone=UTC");

        //设置package信息
        PackageConfig packageConfig = new PackageConfig();
        packageConfig.setParent("com.iurac");
        packageConfig.setModuleName("recruit");

        //设置策略
        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig.setTablePrefix("t_");
        //命名策略为下划线转驼峰。
        strategyConfig.setNaming(NamingStrategy.underline_to_camel);
        strategyConfig.setColumnNaming(NamingStrategy.underline_to_camel);

        //执行
        autoGenerator.setGlobalConfig(globalConfig);
        autoGenerator.setDataSource(dataSourceConfig);
        autoGenerator.setPackageInfo(packageConfig);
        autoGenerator.setStrategy(strategyConfig);
        autoGenerator.execute();

    }
}
