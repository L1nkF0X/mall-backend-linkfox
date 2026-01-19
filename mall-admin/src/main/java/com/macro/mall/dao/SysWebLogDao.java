package com.macro.mall.dao;

import com.macro.mall.model.SysWebLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 后台操作日志Dao
 * Created by macro on 2024/01/19.
 */
@Mapper
public interface SysWebLogDao {
    
    /**
     * 插入操作日志
     */
    int insert(SysWebLog record);
    
    /**
     * 根据ID查询操作日志
     */
    SysWebLog selectByPrimaryKey(Long id);
    
    /**
     * 分页查询操作日志
     */
    List<SysWebLog> selectByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 根据用户名查询操作日志
     */
    List<SysWebLog> selectByUsername(@Param("username") String username);
}
