package com.macro.mall.service;

import com.macro.mall.model.SysWebLog;

import java.util.List;

/**
 * 后台操作日志Service
 * Created by macro on 2024/01/19.
 */
public interface SysWebLogService {
    
    /**
     * 保存操作日志
     */
    int save(SysWebLog webLog);
    
    /**
     * 根据ID查询操作日志
     */
    SysWebLog getById(Long id);
    
    /**
     * 分页查询操作日志
     */
    List<SysWebLog> list(Integer pageNum, Integer pageSize);
    
    /**
     * 根据用户名查询操作日志
     */
    List<SysWebLog> getByUsername(String username);
}
