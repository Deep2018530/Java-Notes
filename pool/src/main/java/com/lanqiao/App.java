package com.lanqiao;

import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        //创建连接池
        DataSourcePool dataSourcePool = new DataSourcePoolImpl();


        for (int i = 0; i < 1000; i++) {
            new Thread(){
                @Override
                public void run() {
                    PooledConnection dataSource = dataSourcePool.getDataSource(); //会总共创建maxSize个连接

                    System.out.print(Thread.currentThread().getName());
                    dataSource.releaseConnection();//释放连接(不是关闭连接，是将状态改为可用)
                }
            }.start();
        }
    }
}
