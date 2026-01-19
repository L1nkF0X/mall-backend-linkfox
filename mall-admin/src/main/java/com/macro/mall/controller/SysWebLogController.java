package com.macro.mall.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.SysWebLog;
import com.macro.mall.service.SysWebLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台操作日志管理Controller
 * Created by macro on 2024/01/19.
 */
@Controller
@Api(tags = "SysWebLogController", description = "后台操作日志管理")
@RequestMapping("/webLog")
public class SysWebLogController {
    
    @Autowired
    private SysWebLogService sysWebLogService;

    @ApiOperation("分页查询操作日志")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<SysWebLog>> list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                    @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        List<SysWebLog> webLogList = sysWebLogService.list(pageNum, pageSize);
        return CommonResult.success(CommonPage.restPage(webLogList));
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
