package com.lanqiao;

import lombok.*;


import java.sql.Connection;

/**
 * 封装连接池中带有连接状态的类
 */
public class PooledConnection {
    private Connection connection;  //连接对象
    private boolean state;      //连接状态

    public PooledConnection(Connection connection, boolean state) {
        this.connection = connection;
        this.state = state;
    }

    /**
     * 释放连接
     */
    public void releaseConnection(){
        System.out.println("连接被释放");
        this.state = false;
    }

    public void setState(boolean state){
        this.state = state;
    }

    public boolean isState() {
        return state;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
