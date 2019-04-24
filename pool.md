实现自己的数据库连接池
===================
什么是数据库连接池？
-----------------

数据库连接池负责分配、管理和释放数据库连接，它允许应用程序重复使用一个现有的数据库连接，而不是再重新建立一个；释放空闲时间超过最大空闲时间的数据库连接来避免因为没有释放数据库连接而引起的数据库连接遗漏。这项技术能明显提高对数据库操作的性能。

## 数据库连接池的几个要点
-------------------

### 数据库信息参数
* 数据库驱动类    
* JDBC连接的URL
* 用户名
* 用户密码
### 数据库连接池参数
* 初始化连接数   `INIT_SIZE`
* 每次新增的连接数 `STEP_SIZE`
* 最大连接数 `MAX_SIZE`
* 超时时间  `TIMEOUT`

### 要点(![代码](https://github.com/Deep2018530/Java-Notes/tree/master/pool/src/main/java/com/lanqiao))
* 初始化连接池
``` JAVA
    transient int initSize = DEFAULT_INIT_SIZE;
    transient int stepSize = DEFAULT_STEP_SIZE;
    transient int maxSize = DEFAULT_MAX_SIZE;
    transient long timeout = DEFAULT_TIMEOUT;

    public DataSourcePoolImpl(){
        //加载驱动
        loadDriver();
        //初始化连接池
        initPool();
    }
  /**
     * 加载驱动(jdbc4.0)
     */
    private void loadDriver() {
       String driverClass = PropertyPlaceHolder.getInstance().getProperty(DRIVER_CLASS);
       try{
           //Class.forName(driverClass);
          /* 对于驱动，在系统的配置里遍历递归找有没有这个驱动，找到了就连接，没找到就报错
             为了不让它去默认递归遍历，非常耗时间，所以这里直接指定这个mysql的驱动，直接注册。不让它去进行默认的配置*/
           Driver driver = (Driver) this.getClass().getClassLoader().loadClass(driverClass).newInstance();
           DriverManager.registerDriver(driver);
       }catch (Exception e){
           e.printStackTrace();
       }

    }

    /**
     *  初始化连接池
     */
    private void initPool() {
        //获取用户用户连接池配置
        String initSizeString = PropertyPlaceHolder.getInstance().getProperty(INIT_SIZE);
        String stepSizeString = PropertyPlaceHolder.getInstance().getProperty(STEP_SIZE);
        String maxSizeString = PropertyPlaceHolder.getInstance().getProperty(MAX_SIZE);
        String timeoutString = PropertyPlaceHolder.getInstance().getProperty(TIMEOUT);

        //处理最后的配置
        initSize = initSizeString == null ? initSize : Integer.parseInt(initSizeString);
        stepSize = stepSizeString == null ? stepSize : Integer.parseInt(stepSizeString);
        maxSize = maxSizeString == null ? maxSize : Integer.parseInt(maxSizeString);
        timeout = timeoutString == null ? timeout : Long.parseLong(timeoutString);

        //初始化连接对象
        try {
            createConnections(initSize);
        } catch (SQLException e) {
            e.printStackTrace();
        }

}
```
* 获取连接对象
  * 判断状态
  * 判断是否可用(连接超时)
``` JAVA
    /**
     * 获取连接池中可用连接对象
     * @return
     */
    private PooledConnection getAvailableConnection() throws SQLException {
        //遍历连接池所有的连接对象
        for (PooledConnection pooledConnection : poolList) {
            //如果连接对象空闲(可用)
            if (!pooledConnection.isState()){
                Connection connection = pooledConnection.getConnection();
                //判断连接对象是否可用
                if (!connection.isValid((int)timeout)){
                    connection = DriverManager.getConnection(
                            PropertyPlaceHolder.getInstance().getProperty(URL),
                            PropertyPlaceHolder.getInstance().getProperty(USER),
                            PropertyPlaceHolder.getInstance().getProperty(PASSWORD));
                    pooledConnection.setConnection(connection);
                }
                pooledConnection.setState(true);
                return pooledConnection;
            }
        }

        return null;
    }
```
* 释放连接对象(并非关闭连接对象，而是把连接对象放回池子或者将连接对象的状态变为`可用、空闲`....等)

