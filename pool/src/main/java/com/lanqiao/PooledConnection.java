package com.lanqiao;

import lombok.*;
import java.sql.Connection;

/**
 * 封装连接池中带有连接状态的类
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PooledConnection {
    private Connection connection;  //连接对象
    private boolean state;      //连接状态

    /**
     * 释放连接
     */
    public void releaseConnection(){
        System.out.println("连接被释放");
        this.state = false;
    }

}
