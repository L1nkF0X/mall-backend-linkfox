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
        long endTime = System.currentTimeMillis();
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method.isAnnotationPresent(WebLog.class)) {
            WebLog webLogAnnotation = method.getAnnotation(WebLog.class);
            webLog.setDescription(webLogAnnotation.description());
        }
        String urlStr = request.getRequestURL().toString();
        webLog.setMethod(request.getMethod());
        webLog.setParams(getParameter(method, joinPoint.getArgs()));
        webLog.setCreateTime(new Date());
        webLog.setSpendTime((int) (endTime - startTime));
        webLog.setIp(RequestUtil.getRequestIp(request));
        webLog.setUsername(getCurrentUsername(request, joinPoint.getArgs()));
        //异步保存日志到数据库
        sysWebLogService.saveAsync(webLog);
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
     * 优先从Token中获取，如果获取不到则尝试从请求参数中提取
     */
    private String getCurrentUsername(HttpServletRequest request, Object[] args) {
        // 1. 优先尝试从Token中获取用户名
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
                String username = claims.getSubject();
                if (StrUtil.isNotEmpty(username) && !"anonymous".equals(username)) {
                    return username;
                }
            } catch (Exception e) {
                LOGGER.debug("JWT token解析失败，尝试从请求参数中获取用户名", e);
            }
        }
        
        // 2. Token获取失败时，尝试从请求参数中获取用户名
        String usernameFromParams = extractUsernameFromRequest(request);
        if (StrUtil.isNotEmpty(usernameFromParams)) {
            return usernameFromParams;
        }
        
        // 3. 从方法参数中提取用户名
        String usernameFromArgs = extractUsernameFromArgs(args);
        if (StrUtil.isNotEmpty(usernameFromArgs)) {
            return usernameFromArgs;
        }
        
        return "anonymous";
    }
    
    /**
     * 从请求参数中提取用户名
     * 支持表单参数和JSON参数
     */
    private String extractUsernameFromRequest(HttpServletRequest request) {
        try {
            // 1. 尝试从表单参数中获取
            String username = request.getParameter("username");
            if (StrUtil.isNotEmpty(username)) {
                LOGGER.debug("从表单参数中获取到用户名: {}", username);
                return username;
            }
            
            // 2. 尝试从请求体中获取（针对JSON请求）
            // 注意：这里只是示例，实际项目中可能需要根据具体的请求体格式来解析
            // 由于HttpServletRequest的输入流只能读取一次，这里采用更安全的方式
            
        } catch (Exception e) {
            LOGGER.debug("从请求参数中提取用户名失败", e);
        }
        
        return null;
    }
    
    /**
     * 从方法参数中提取用户名
     * 当Token无法获取用户名时，尝试从方法参数中提取
     */
    private String extractUsernameFromArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        
        try {
            // 遍历所有参数，寻找包含username字段的对象
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                
                // 尝试将参数转换为JSON并提取username
                String jsonStr = JSONUtil.toJsonStr(arg);
                if (StrUtil.isNotEmpty(jsonStr) && jsonStr.contains("username")) {
                    try {
                        cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(jsonStr);
                        if (jsonObject.containsKey("username")) {
                            String username = jsonObject.getStr("username");
                            if (StrUtil.isNotEmpty(username)) {
                                LOGGER.debug("从方法参数中提取到用户名: {}", username);
                                return username;
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.debug("解析参数JSON失败: {}", jsonStr, e);
                    }
                }
                
                // 尝试通过反射获取username字段
                try {
                    java.lang.reflect.Field usernameField = arg.getClass().getDeclaredField("username");
                    usernameField.setAccessible(true);
                    Object usernameValue = usernameField.get(arg);
                    if (usernameValue != null && StrUtil.isNotEmpty(usernameValue.toString())) {
                        LOGGER.debug("通过反射从参数中提取到用户名: {}", usernameValue);
                        return usernameValue.toString();
                    }
                } catch (Exception e) {
                    // 忽略反射异常，继续尝试其他参数
                }
            }
        } catch (Exception e) {
            LOGGER.debug("从方法参数中提取用户名失败", e);
        }
        
        return null;
    }

}
