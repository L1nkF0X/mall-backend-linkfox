# AOP操作日志功能使用说明

## 功能概述
本项目已集成AOP操作日志功能，可以自动记录用户的操作行为到数据库中，包括操作人、IP地址、请求方法、参数、操作描述、操作时间和耗时等信息。

## 数据库表结构
```sql
DROP TABLE IF EXISTS `sys_web_log`;
CREATE TABLE `sys_web_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) DEFAULT NULL COMMENT '操作人用户名',
  `ip` varchar(64) DEFAULT NULL COMMENT 'IP地址',
  `method` varchar(200) DEFAULT NULL COMMENT '调用的方法',
  `params` text COMMENT '请求参数',
  `description` varchar(255) DEFAULT NULL COMMENT '操作描述',
  `create_time` datetime DEFAULT NULL COMMENT '操作时间',
  `spend_time` int(11) DEFAULT NULL COMMENT '消耗时间(毫秒)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台操作日志表';
```

## 使用方法

### 1. 在Controller方法上添加@WebLog注解
```java
@ApiOperation("修改指定用户信息")
@RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
@ResponseBody
@WebLog(description = "修改用户信息")
public CommonResult update(@PathVariable Long id, @RequestBody UmsAdmin admin) {
    // 业务逻辑
}
```

### 2. 注解参数说明
- `description`: 操作描述，用于说明该操作的业务含义

### 3. 自动记录的信息
- **username**: 当前登录用户名（从JWT token中解析）
- **ip**: 客户端IP地址
- **method**: HTTP请求方法（GET、POST等）
- **params**: 请求参数（JSON格式）
- **description**: 操作描述（来自@WebLog注解）
- **create_time**: 操作时间
- **spend_time**: 方法执行耗时（毫秒）

## 查询日志接口

### 1. 分页查询操作日志
```
GET /webLog/list?pageNum=1&pageSize=10
```

### 2. 根据ID查询操作日志
```
GET /webLog/{id}
```

### 3. 根据用户名查询操作日志
```
GET /webLog/listByUsername?username=admin
```

## 配置说明

### 1. JWT相关配置
确保application.yml中配置了JWT相关参数：
```yaml
jwt:
  tokenHeader: Authorization
  secret: mySecret
  expiration: 604800
  tokenHead: Bearer
```

### 2. AOP依赖
项目已包含spring-boot-starter-aop依赖，无需额外配置。

## 核心组件

### 1. 注解类
- `@WebLog`: 用于标记需要记录日志的方法

### 2. 切面类
- `WebLogAspect`: AOP切面，负责拦截带有@WebLog注解的方法并记录日志

### 3. 实体类
- `SysWebLog`: 操作日志实体类

### 4. 数据访问层
- `SysWebLogDao`: 操作日志数据访问接口
- `SysWebLogDao.xml`: MyBatis映射文件

### 5. 业务层
- `SysWebLogService`: 操作日志业务接口
- `SysWebLogServiceImpl`: 操作日志业务实现类

### 6. 控制层
- `SysWebLogController`: 操作日志查询接口

## 使用示例

已在`UmsAdminController`中添加了使用示例：
- 用户注册
- 用户登录
- 修改用户信息
- 修改用户密码
- 删除用户
- 修改用户状态
- 分配用户角色

## 注意事项

1. 只有添加了`@WebLog`注解的方法才会被记录日志
2. 用户名通过JWT token解析获取，未登录用户显示为"anonymous"
3. 请求参数会被序列化为JSON格式存储
4. 日志记录是异步进行的，不会影响业务方法的执行性能
5. 建议在重要的业务操作方法上添加该注解，如增删改操作

## 问题修复说明

在实现过程中，发现项目原本存在一个用于Logstash日志记录的WebLogAspect类，与我们新建的数据库操作日志功能发生冲突。已进行以下修复：

1. **删除了原有的冲突文件**：
   - `mall-common/src/main/java/com/macro/mall/common/log/WebLogAspect.java`
   - `mall-common/src/main/java/com/macro/mall/common/domain/WebLog.java`

2. **保留了新的AOP操作日志功能**：
   - 使用`@WebLog`注解标记需要记录的方法
   - 自动保存操作日志到数据库表`sys_web_log`
   - 提供完整的日志查询接口

3. **复用了现有工具类**：
   - 使用`RequestUtil.getRequestIp()`获取客户端IP地址
   - 保持了与项目现有架构的一致性

现在项目可以正常启动，不会再出现BeanDefinitionOverrideException错误。
