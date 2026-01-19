package com.macro.mall.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.macro.mall.annotation.WebLog;
import com.macro.mall.common.util.RequestUtil;
import com.macro.mall.model.SysWebLog;
import com.macro.mall.service.SysWebLogService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * 统一日志处理切面
 * Created by macro on 2024/01/19.
 */
@Aspect
@Component
public class WebLogAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebLogAspect.class);
    
    @Autowired
    private SysWebLogService sysWebLogService;
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Pointcut("@annotation(com.macro.mall.annotation.WebLog)")
    public void webLog() {
    }

    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        //获取当前请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        //记录请求信息
        SysWebLog webLog = new SysWebLog();
        Object result = joinPoint.proceed();
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method.isAnnotationPresent(WebLog.class)) {
            WebLog webLogAnnotation = method.getAnnotation(WebLog.class);
            webLog.setDescription(webLogAnnotation.description());
        }
        long endTime = System.currentTimeMillis();
        String urlStr = request.getRequestURL().toString();
        webLog.setMethod(request.getMethod());
        webLog.setParams(getParameter(method, joinPoint.getArgs()));
        webLog.setCreateTime(new Date());
        webLog.setSpendTime((int) (endTime - startTime));
        webLog.setIp(RequestUtil.getRequestIp(request));
        webLog.setUsername(getCurrentUsername(request));
        //保存日志到数据库
        sysWebLogService.save(webLog);
        LOGGER.info("{}", JSONUtil.parse(webLog));
        return result;
    }

    /**
     * 根据方法和传入的参数获取请求参数
     */
    private String getParameter(Method method, Object[] args) {
        StringBuilder sb = new StringBuilder();
        String[] parameterNames = new String[method.getParameterCount()];
        for (int i = 0; i < method.getParameterCount(); i++) {
            parameterNames[i] = "arg" + i;
        }
        if (args != null && parameterNames != null) {
            for (int i = 0; i < args.length; i++) {
                sb.append(parameterNames[i]).append(": ").append(JSONUtil.toJsonStr(args[i])).append("; ");
            }
        }
        return sb.toString();
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        if (token != null && token.startsWith(tokenHead)) {
            token = token.substring(tokenHead.length());
        }
        if (StrUtil.isNotEmpty(token)) {
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(secret)
                        .parseClaimsJws(token)
                        .getBody();
                return claims.getSubject();
            } catch (Exception e) {
                LOGGER.warn("JWT token解析失败", e);
            }
        }
        return "anonymous";
    }

}
