package com.macro.mall.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.SysWebLog;
import com.macro.mall.service.SysWebLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台操作日志管理Controller
 */
@Controller
@Api(tags = "SysWebLogController", description = "后台操作日志管理")
@RequestMapping("/webLog")
public class SysWebLogController {
    
    @Autowired
    private SysWebLogService sysWebLogService;

    @ApiOperation("分页查询操作日志（支持多条件筛选）")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<Map<String, Object>> list(
            @ApiParam("当前页码") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @ApiParam("每页显示数量") @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
            @ApiParam("用户名（模糊匹配）") @RequestParam(value = "username", required = false) String username,
            @ApiParam("请求方法") @RequestParam(value = "method", required = false) String method) {
        
        Map<String, Object> result = sysWebLogService.listByCondition(username, method, pageNum, pageSize);
        return CommonResult.success(result);
    }

    @ApiOperation("根据ID查询操作日志")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<SysWebLog> getItem(@PathVariable Long id) {
        SysWebLog webLog = sysWebLogService.getById(id);
        return CommonResult.success(webLog);
    }

    @ApiOperation("根据用户名查询操作日志")
    @RequestMapping(value = "/listByUsername", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<SysWebLog>> listByUsername(@RequestParam String username) {
        List<SysWebLog> webLogList = sysWebLogService.getByUsername(username);
        return CommonResult.success(webLogList);
    }
}
