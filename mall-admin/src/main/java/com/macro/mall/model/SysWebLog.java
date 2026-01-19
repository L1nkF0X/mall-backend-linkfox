package com.macro.mall.model;

import lombok.Data;

import java.util.Date;

/**
 * 后台操作日志实体类
 * Created by macro on 2024/01/19.
 */
@Data
public class SysWebLog {
    private Long id;
    
    /**
     * 操作人用户名
     */
    private String username;
    
    /**
     * IP地址
     */
    private String ip;
    
    /**
     * 调用的方法
     */
    private String method;
    
    /**
     * 请求参数
     */
    private String params;
    
    /**
     * 操作描述
     */
    private String description;
    
    /**
     * 操作时间
     */
    private Date createTime;
    
    /**
     * 消耗时间(毫秒)
     */
    private Integer spendTime;
}
