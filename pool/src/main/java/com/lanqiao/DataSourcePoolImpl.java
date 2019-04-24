package com.lanqiao;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataSourcePoolImpl extends PoolConstant implements  DataSourcePool{

    //连接池 读多写少

    private List<PooledConnection> poolList = new CopyOnWriteArrayList<PooledConnection>();
    private Lock lock = new ReentrantLock();


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

    /**
     * 创建连接对象的方法
     */
    private void createConnections(int count) throws SQLException {
        if (poolList.size() + count <= maxSize){
                for (int i = 0; i < count; i++) {
                    System.out.println("创建了连接==" +i+"====>");

                    //创建一个数据库连接对象
                    Connection connection = DriverManager.getConnection(
                            PropertyPlaceHolder.getInstance().getProperty(URL),
                            PropertyPlaceHolder.getInstance().getProperty(USER),
                            PropertyPlaceHolder.getInstance().getProperty(PASSWORD));
                    //创建数据库连接池的连接对象
                    PooledConnection pooledConnection = new PooledConnection(connection, false);
                    poolList.add(pooledConnection);
                }
        }
    }

    /**
     * 加载驱动(jdbc4.0)
     */
    private void loadDriver() {
       String driverClass = PropertyPlaceHolder.getInstance().getProperty(DRIVER_CLASS);
       try{
           Class.forName(driverClass);
          /* 对于驱动，在系统的配置里遍历递归找有没有这个驱动，找到了就连接，没找到就报错
             为了不让它去默认递归遍历，非常耗时间，所以这里直接指定这个mysql的驱动，直接注册。不让它去进行默认的配置*/
           Driver driver = (Driver) this.getClass().getClassLoader().loadClass(driverClass).newInstance();
           DriverManager.registerDriver(driver);
       }catch (Exception e){
           e.printStackTrace();
       }

    }


    @Override
      public PooledConnection getDataSource() {
        PooledConnection connection = null;
        try{
            lock.lock();
            connection = getAvailableConnection();

            //没拿到可用的连接对象
            while(connection == null){
                //继续创建连接对象(创建数量为)
                createConnections(stepSize);
                connection = getAvailableConnection();

                //如果还没有拿到，过60毫秒再去要
                if (connection == null){
                    TimeUnit.MILLISECONDS.sleep(60);
                }

            }
        }catch(Exception e){
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return connection;
    }

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
}
