package com.neko233.hotdeploy.util;

/**
 * @author SolarisNeko
 * Date on 2022-06-21
 */
public class SplitUtil {

    /**
     * @param className full class name
     * @return beanName - spring style
     */
    public static String getBeanNameByClassName(String className) {
        String[] split = className.split("\\.");
        String simpleClassName = split[split.length - 1];
        return toLowerCamelCaseName(simpleClassName);
    }


    //首字母小写
    public static String toLowerCamelCaseName(String name) {
        name = name.substring(0, 1).toLowerCase() + name.substring(1);
        return name;
    }
}
