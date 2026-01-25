package com.macro.mall.service;

import com.macro.mall.model.SysWebLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 后台操作日志Service
 */
public interface SysWebLogService {
    
    /**
     * 保存操作日志
     */
    int save(SysWebLog webLog);
    
    /**
     * 异步保存操作日志
     */
    CompletableFuture<Void> saveAsync(SysWebLog webLog);
    
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
    
    /**
     * 多条件分页查询操作日志
     */
    Map<String, Object> listByCondition(String username, String method, Integer pageNum, Integer pageSize);
}
