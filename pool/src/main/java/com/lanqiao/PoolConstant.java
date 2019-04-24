package com.lanqiao;

public class PoolConstant {
    public static final String DRIVER_CLASS = "jdbc.driver";
    public static final String URL = "jdbc.url";
    public static final String USER = "jdbc.user";
    public static final String PASSWORD = "jdbc.password";
    public static final String INIT_SIZE = "initSize";
    public static final String STEP_SIZE = "stepSize";
    public static final String MAX_SIZE = "maxSize";
    public static final String TIMEOUT = "timeout";

    //默认配置
    public static final int DEFAULT_INIT_SIZE = 10;
    public static final int DEFAULT_STEP_SIZE = 4;
    public static final int DEFAULT_MAX_SIZE = 30;
    public static final long DEFAULT_TIMEOUT = 5000;
}
