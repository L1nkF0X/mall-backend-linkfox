package com.macro.mall.service.impl;

import com.macro.mall.dao.SysWebLogDao;
import com.macro.mall.model.SysWebLog;
import com.macro.mall.service.SysWebLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台操作日志Service实现类
 * Created by macro on 2024/01/19.
 */
@Service
public class SysWebLogServiceImpl implements SysWebLogService {
    
    @Autowired
    private SysWebLogDao sysWebLogDao;
    
    @Override
    public int save(SysWebLog webLog) {
        return sysWebLogDao.insert(webLog);
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
}
