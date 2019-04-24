package com.lanqiao;

import java.io.InputStream;
import java.util.Properties;

/**
 * 解析dataSource.properties
 * 使用单例设计属性文件的操作类
 */
public class PropertyPlaceHolder extends Properties {

    public static final String CONFIG_LOCATION = "dataSource.properties";

    private static PropertyPlaceHolder holder = new PropertyPlaceHolder();

    private PropertyPlaceHolder(){
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(CONFIG_LOCATION)){
            this.load(in);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public static PropertyPlaceHolder getInstance(){
        return holder;
    }
}
