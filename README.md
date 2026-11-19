# 微服务架构医院挂号与问诊系统（hms-cloud）

珠海科技学院计算机学院《微服务架构》B 类实验 · 题目 9

## 一、技术栈

| 组件 | 版本 / 选型 | 说明 |
|---|---|---|
| Spring Boot | 2.3.7.RELEASE | 微服务基础框架 |
| Spring Cloud | Hoxton.SR5 | 微服务治理全家桶 |
| Eureka | Netflix Eureka | 服务注册与发现 |
| Spring Cloud Gateway | 2.2.3 | 统一网关：路由 / 鉴权 / 限流 |
| Spring Cloud Config | 2.2.3 | 分布式配置中心（Git 仓库存储） |
| OpenFeign + Ribbon | 2.2.3 | 声明式服务调用与客户端负载均衡 |
| Hystrix + Dashboard | 1.5.18 | 熔断降级与监控 |
| MyBatis | 2.1.4 | 持久层（XML 映射） |
| MySQL | 8.0 | 每服务独立数据库 |
| JWT (jjwt) | 0.9.1 | 令牌认证 |

## 二、服务清单

| 服务 | 端口 | 独立数据库 | 职责 |
|---|---|---|---|
| hms-eureka-server | 8761 | - | 服务注册中心 |
| hms-config-server | 8888 | - | 配置中心（读取 config-repo Git 仓库） |
| hms-gateway-server | 8500 | - | 网关 + 前端静态资源托管 |
| hms-user-service | 8071 | hms_user | 用户 / 认证（患者、医生、管理员） |
| hms-department-service | 8072 | hms_department | 科室与医生信息 |
| hms-schedule-service | 8073 | hms_schedule | 医生排班与号源（行锁防超卖） |
| hms-register-service | 8074 | hms_register | 挂号编排核心 |
| hms-record-service | 8075 | hms_record | 电子病历与在线问诊 |
| hms-hystrix-dashboard | 8070 | - | 熔断监控面板 |

## 三、运行前准备

1. JDK 8、Maven 3.6+、MySQL 8.0 已安装且 MySQL 服务已启动。
2. 初始化数据库（root 密码按本机实际修改）：

```bash
mysql -uroot -p12345678 < docs/sql/init.sql
```

3. 如 MySQL 账号密码与默认不同，修改 `config-repo/hms-*-service.yml` 中的 `spring.datasource`。

## 四、构建与启动

```bash
# 构建（跳过测试）
mvn clean package -DskipTests

# 一键启动（Windows）
run-all.bat

# 一键停止
stop-all.bat
```

启动完成后访问：

- 前端系统：<http://localhost:8500/>
- 注册中心：<http://localhost:8761/>
- 配置中心：<http://localhost:8888/hms-user-service/default>
- 熔断监控：<http://localhost:8070/hystrix>，监控流填 <http://localhost:8074/actuator/hystrix.stream>

## 五、演示账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 患者 | patient1 / patient2 / patient3 | 123456 |
| 医生 | D001 ~ D008 | 123456 |
| 管理员 | admin | 123456 |

## 六、核心业务链路

挂号：前端 → Gateway（JWT 鉴权 + 令牌桶限流 10 次/秒）→ Register Service
→ ① Feign 调 Schedule 扣减号源（`SELECT ... FOR UPDATE` 行锁）
→ ② Feign 调 User 校验患者 → ③ Feign 调 Department 取医生信息
→ ④ 本地事务落库挂号单 → ⑤ Feign 调 Record 建病历；
任一步失败则回滚本地事务并调用 Schedule `restore` 归还号源（补偿机制）。

退号：置状态为已退号 + Feign 归还号源。

## 七、目录说明

```
hms-cloud
├── config-repo           # 配置中心 Git 仓库（application.yml + 各服务配置）
├── docs/sql/init.sql     # 数据库初始化脚本
├── hms-common            # 公共模块（Result / DTO / JWT / 全局异常）
├── hms-eureka-server     # 注册中心
├── hms-config-server     # 配置中心
├── hms-gateway-server    # 网关 + static 前端
├── hms-user-service      # 用户认证
├── hms-department-service# 科室医生
├── hms-schedule-service  # 排班号源
├── hms-register-service  # 挂号编排
├── hms-record-service    # 病历问诊
└── hms-hystrix-dashboard # 熔断监控
```
