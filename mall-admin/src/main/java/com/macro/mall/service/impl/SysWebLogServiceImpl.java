package com.macro.mall.service.impl;

import com.macro.mall.dao.SysWebLogDao;
import com.macro.mall.model.SysWebLog;
import com.macro.mall.service.SysWebLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 后台操作日志Service实现类
 */
@Service
public class SysWebLogServiceImpl implements SysWebLogService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SysWebLogServiceImpl.class);
    
    @Autowired
    private SysWebLogDao sysWebLogDao;
    
    @Override
    public int save(SysWebLog webLog) {
        return sysWebLogDao.insert(webLog);
    }
    
    @Override
    @Async("logTaskExecutor")
    public CompletableFuture<Void> saveAsync(SysWebLog webLog) {
        try {
            sysWebLogDao.insert(webLog);
            LOGGER.debug("异步保存操作日志成功，用户：{}，操作：{}", webLog.getUsername(), webLog.getDescription());
        } catch (Exception e) {
            LOGGER.error("异步保存操作日志失败，用户：{}，操作：{}，错误：{}", 
                webLog.getUsername(), webLog.getDescription(), e.getMessage(), e);
        }
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public SysWebLog getById(Long id) {
        return sysWebLogDao.selectByPrimaryKey(id);
    }
    
    @Override
    public List<SysWebLog> list(Integer pageNum, Integer pageSize) {
        Integer offset = (pageNum - 1) * pageSize;
        return sysWebLogDao.selectByPage(offset, pageSize);
    }
    
    @Override
    public List<SysWebLog> getByUsername(String username) {
        return sysWebLogDao.selectByUsername(username);
    }
    
    @Override
    public Map<String, Object> listByCondition(String username, String method, Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算偏移量
        Integer offset = (pageNum - 1) * pageSize;
        
        // 查询数据列表
        List<SysWebLog> list = sysWebLogDao.selectByCondition(username, method, offset, pageSize);
        
        // 查询总数
        Long total = sysWebLogDao.countByCondition(username, method);
        
        // 计算总页数
        Long totalPage = (total + pageSize - 1) / pageSize;
        
        // 封装返回结果
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        result.put("totalPage", totalPage);
        result.put("total", total);
        result.put("list", list);
        
        return result;
    }
}
