
- [简介](#简介)
- [功能说明](#功能说明)
- [构建](#构建)
  - [环境要求](#环境要求)
  - [依赖包的安装](#依赖包的安装)
  - [打发布包](#打发布包)
  - [发布到仓库](#发布到仓库)
- [安装](#安装)
- [使用](#使用)
- [项目结构说明](#项目结构说明)
- [FAQ](#faq)
  - [访客功能注意点](#访客功能注意点)
  - [兼容说明](#兼容说明)
- [相关链接](#相关链接)
- [支持](#支持)


## 简介


前端采用Vue、Element UI。
后端采用Spring Boot、Spring Security、Redis & Jwt。
权限认证使用Jwt，支持多终端认证系统。
支持加载动态权限菜单，多方式轻松权限控制。
高效率开发，使用代码生成器可以一键生成前后端代码。

## 功能说明
1.  用户管理：用户是系统操作者，该功能主要完成系统用户配置。
2.  部门管理：配置系统组织机构（公司、部门、小组），树结构展现支持数据权限。
3.  岗位管理：配置系统用户所属担任职务。
4.  菜单管理：配置系统菜单，操作权限，按钮权限标识等。
5.  角色管理：角色菜单权限分配、设置角色按机构进行数据范围权限划分。
6.  字典管理：对系统中经常使用的一些较为固定的数据进行维护。
7.  参数管理：对系统动态配置常用参数。
8.  通知公告：系统通知公告信息发布维护。
9.  操作日志：系统正常操作日志记录和查询；系统异常信息日志记录和查询。
10. 登录日志：系统登录日志记录查询包含登录异常。
11. 在线用户：当前系统中活跃用户状态监控。
12. 定时任务：在线（添加、修改、删除)任务调度包含执行结果日志。
13. 代码生成：前后端代码的生成（java、html、xml、sql）支持CRUD下载 。
14. 系统接口：根据业务代码自动生成相关的api接口文档。
15. 服务监控：监视当前系统CPU、内存、磁盘、堆栈等相关信息。
16. 在线构建器：拖动表单元素生成相应的HTML代码。
17. 连接池监视：监视当前系统数据库连接池状态，可进行分析SQL找出系统性能瓶颈。
## 构建 


### 环境要求

运行环境要求:

- JDK 8+
- Centos7+
- Ubuntu14 +
- MySQL 5.7 +
- Redis 5 +
- Nginx
- Maven

编译要求:

- JDK 8+
- Centos7 +
- Ubuntu14 +

### 依赖包的安装
请在联网环境下进行上述运行环境软件的安装，了相关依赖包可以自动安装


### 打发布包


```shell
mvn clean package
```
jar包位置为 

```
eyecool-biapwp-admin/target/eyecool-biapwp-admin-{{version}}.jar
```

### 发布到仓库


[_归档地址_](http://192.168.60.66/library/fbc40c57-39e4-4902-96db-6cc0ded506ec/%E4%BA%A7%E5%93%81%E5%8F%8A%E9%A1%B9%E7%9B%AE%E7%89%88%E6%9C%AC%E8%BF%87%E7%A8%8B%E6%B8%85%E5%8D%95/%202019012805_Saas%E5%B9%B3%E5%8F%B0)

## 安装 
[部署手册（需要解压）](http://192.168.60.66/f/c4b965478cda4555a2a4/)

nginx增加配置

```
    server {
        listen      8700;
        server_name localhost;

        location / {
            root   /home/eyecool/biapwp-assps/biapwp-assps-ui; #前端文件路径
            try_files $uri $uri/ /index.html;
            index  index.html index.htm;
        }


        location /prod-api/{
            proxy_set_header Host $http_host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header REMOTE-HOST $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_pass http://localhost:8701/;
            client_max_body_size 500m;
        }


        location /websocket/{
            proxy_read_timeout 3600s;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection $connection_upgrade;
            proxy_pass http://localhost:8701/websocket/;
        }

        location /api/ws/ {
            proxy_pass http://192.168.63.6:9999/api/ws/;
            proxy_read_timeout 3600s;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection $connection_upgrade;
        }


         location /api/websocket/ {
            proxy_pass http://127.0.0.1:8701/api/websocket/;
            proxy_read_timeout 3600s;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection $connection_upgrade;
        }



        location /api/ {
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header Host $http_host;
            proxy_set_header X-Forwarded-Proto  $scheme;
            proxy_set_header REMOTE-HOST $remote_addr;
            proxy_pass http://127.0.0.1:8701/api/;
            client_max_body_size 500m;
        }



        location /visitor {
            alias /home/eyecool/biapwp-assps/biapwp-assps-visitor-ui;##访客页面路径
            index  index.html index.htm;
        }

        location /show_view {
           alias /home/eyecool/biapwp-assps/eyecool-popwin-view;##无感弹窗页面路径
           index view-stanadrd.html;
        }

        error_page 500 502 503 504  /50x.html;
        location = /50x.html {
            root html;
        }
    }

```
## 使用

将jar包和启动脚本 _eyecool-biapwp-server.sh_ 放在同一目录下

```sh
./eyecool-biapwp-server.sh start
```

## 项目结构说明

```
├───emqx-spring-boot-autoconfigure
│   └───src/main
│           ├───java/cn/eyecool
│           │           └───emqx emqx自动配置
│           └───resources   配置文件
│                       
├───eyecool-biapwp-adapter  203设备接入适配
│   ├───src/main
│   │       ├───java/cn/eyecool/device
│   │       │               ├───constant  相关常量
│   │       │               ├───controller  功能接口
│   │       │               ├───domain  对象
│   │       │               ├───event mqtt事件
│   │       │               ├───mapper  mybatis接口
│   │       │               ├───service 服务和实现
│   │       │               ├───task  定时任务
│   │       │               ├───util  工具类
│   │       │               └───vo  数据对象
│   │       │                       
│   │       └───resources/mapper    mybatis配置
│   └───target
├───eyecool-biapwp-admin  web服务入口
│   ├───config  配置文件
│   ├───src
│   │   └───main
│   │       ├───java/cn/eyecool 启动类
│   │       │          ├───EyecoolApplication.java
│   │       │          ├───EyecoolServletInitializer.java
│   │       │          └───web
│   │       │              ├───controller  web服务接口
│   │       │              └───core  swagger配置
│   │       └───resources 配置文件
│   └───target
├───eyecool-biapwp-area 区域管理模块
│   ├───src/main
│   │       ├───java/cn/eyecool/area
│   │       │               ├───controller  区域接口
│   │       │               ├───domain  区域对象
│   │       │               ├───mapper  mybatis接口
│   │       │               └───service 服务和实现
│   │       └───resources/mapper    mybatis配置
│   └───target
├───eyecool-biapwp-attendance 考勤模块
│   ├───src/main
│   │       ├───java/cn/eyecool/attendance
│   │       │               ├───constant  考勤常量
│   │       │               ├───controller  功能接口
│   │       │               ├───domain  考勤对象
│   │       │               ├───job 定时任务
│   │       │               ├───mapper  mybatis接口
│   │       │               ├───service 服务和实现
│   │       │               └───utils 工具类
│   │       └───resources/mapper    mybatis配置
│   │                           
│   └───target
├───eyecool-biapwp-basedata 基础数据
│   ├───src/main
│   │       ├───java/cn/eyecool/basedata
│   │       │               ├───constant  常量
│   │       │               ├───controller  基础数据接口
│   │       │               ├───domain  基础数据对象
│   │       │               ├───enums 基础数据枚举
│   │       │               ├───event mqtt时间
│   │       │               ├───manager 基础数据ABIS相关服务和实现
│   │       │               ├───mapper  mybatis接口
│   │       │               ├───service 业务服务和实现
│   │       │               ├───task  定时任务
│   │       │               └───vo  数据对象
│   │       └───resources/mapper    mybatis配置
│   └───target
├───eyecool-biapwp-common 通用模块
│   ├───src/main/java
│   │       ├───cn/eyecool/common
│   │       │        ├───annotation  通用注解
│   │       │        ├───config  项目配置读取
│   │       │        │   └───tenant  租户配置读取
│   │       │        ├───constant  通用常量
│   │       │        ├───context 租户线程缓存
│   │       │        ├───core
│   │       │        │   ├───controller  通用接口
│   │       │        │   ├───domain  通用对象
│   │       │        │   ├───page  分页对象
│   │       │        │   ├───redis redis配置和工具
│   │       │        │   └───text  文本处理
│   │       │        ├───enums 通用枚举
│   │       │        ├───exception 通用异常处理
│   │       │        ├───filter  过滤器
│   │       │        ├───json  JSON解析处理
│   │       │        ├───match 本地多模态比对服务
│   │       │        ├───mqtt  mqtt配置
│   │       │        └───utils 通用工具
│   │       └───com/github/pagehelper/dialect/helper    分页配置
│   └───target
├───eyecool-biapwp-device 设备管理
│   ├───src/main
│   │       ├───java/cn/eyecool/device
│   │       │       ├───constant  常量
│   │       │       ├───controller  功能接口
│   │       │       ├───domain  设备对象
│   │       │       ├───event 设备操作事件监听
│   │       │       ├───mapper mybatis接口
│   │       │       ├───proto proto3定义
│   │       │       ├───service 服务和实现
│   │       │       └───task  定时任务
│   │       └───resources/mapper    mybatis配置
│   └───target
├───eyecool-biapwp-framework  核心框架
│   ├───src/main/java/cn/eyecool/framework
│   │    ├───aspectj 通用切面处理
│   │    ├───config  框架配置
│   │    ├───datasource  动态数据库配置
│   │    ├───interceptor 通用拦截器
│   │    ├───manager 异步功能
│   │    ├───security security框架配置
│   │    └───web 系统监控
│   └───target
├───eyecool-biapwp-generator  代码生成
│   ├───src/main
│   │   ├───java/cn/eyecool/generator
│   │   └───resources/mapper    mybatis配置
│   └───target
├───eyecool-biapwp-healthcode 健康码模块
│   ├───src/main
│   │     ├───java/cn/eyecool/healthcode
│   │     │     ├───constant  常量
│   │     │     ├───param 请求参数
│   │     │     └───service 服务和实现
│   │     └───resources/mapper    mybatis配置
│   └───target
├───eyecool-biapwp-msg  消息推送模块
│   ├───src/main
│   │       ├───java/cn/eyecool/msg
│   │       │      ├───configure
│   │       │      │   ├───ding  钉钉配置
│   │       │      │   ├───mail  优先配置
│   │       │      │   └───weixin  公众号配置
│   │       │      ├───constant  常量
│   │       │      ├───controller  功能接口
│   │       │      ├───domain  消息对象
│   │       │      ├───mapper  mybatis接口
│   │       │      ├───service 服务及实现
│   │       │      ├───task  定时任务
│   │       │      ├───trade 三方平台交互
│   │       │      └───util  工具类
│   │       └───resources/mapper    mybatis配置
│   └───target
├───eyecool-biapwp-non-inductive  无感识别模块
│   ├───src/main
│   │       ├───java/cn/eyecool/noninductive
│   │       │       ├───controller  功能接口
│   │       │       │       NonInductiveDeviceInfoController.java
│   │       │       ├───disruptor 异步队列
│   │       │       ├───domain  无感功能对象
│   │       │       ├───mapper  mybatis接口
│   │       │       ├───sdk
│   │       │       │   └───HCNetCamera 测温JNA
│   │       │       ├───service 接口及实现
│   │       │       ├───web
│   │       │       │   └───controller  设备通讯接口
│   │       │       └───websocket 无感socket配置
│   │       └───resources
│   │           ├───lib 测温SDK
│   │           └───mapper    mybatis配置
│   └───target
├───eyecool-biapwp-ocr  ocr识别模块
│   ├───src/main
│   │       ├───java/cn/eyecool/ocr
│   │       │     ├───controller  功能模块
│   │       │     ├───domain  ocr对象
│   │       │     ├───mapper  mybatis接口
│   │       │     └───service 服务及实现
│   │       └───resources/mapper    mybatis配置
│   └───target
├───eyecool-biapwp-quartz 定时任务框架
│   ├───src/main
│   │       ├───java/cn/eyecool/quartz
│   │       │      ├───config  框架配置
│   │       │      ├───controller  功能接口
│   │       │      ├───domain  任务对象
│   │       │      ├───mapper  mybatis接口
│   │       │      ├───service 服务及实现
│   │       │      ├───task  定时任务
│   │       │      └───util  工具类
│   │       └───resources/mapper    mybatis配置
│   └───target
├───eyecool-biapwp-scene  场景功能模块
│   ├───src/main
│   │       ├───java/cn/eyecool/scene
│   │       │      ├───constant  常量
│   │       │      ├───controller  功能接口
│   │       │      ├───domain  场景对象
│   │       │      ├───enums 场景枚举
│   │       │      ├───event 场景监听事件
│   │       │      ├───mapper  mybatis接口
│   │       │      ├───service 服务及实现
│   │       │      └───trade
│   │       │            ├───entity  场景业务对象
│   │       │            ├───service 场景业务服务及实现
│   │       │            └───vo  场景数据对象
│   │       └───resources/mapper    mybatis配置
│   └───target
├───eyecool-biapwp-server HTTP API 接口服务
│   ├───src/main/java/cn/eyecool/server
│   │               ├───annotation  服务注解
│   │               ├───aspectj 切面处理
│   │               ├───handler API处理方法
│   │               ├───http  http请求配置
│   │               └───mqtt mqtt配置
│   │                               
│   └───target
├───eyecool-biapwp-statistic  统计模块
│   ├───src/main
│   │       ├───java/cn/eyecool/statistic
│   │       │        ├───constant  常量
│   │       │        ├───controller  功能接口
│   │       │        ├───domain  统计对象
│   │       │        ├───mapper  mybatis接口
│   │       │        ├───service 服务及实现
│   │       │        └───task  定时任务
│   │       └───resources/mapper    mybatis配置
│   └───target
├───eyecool-biapwp-system 系统功能模块
│   ├───src/main
│   │       ├───java/cn/eyecool/system
│   │       │      ├───constant  常量
│   │       │      ├───domain  系统功能对象
│   │       │      │   └───vo  数据对象
│   │       │      ├───mapper  mybatis接口
│   │       │      ├───service 服务及实现
│   │       │      └───util  工具类
│   │       └───resources/mapper    mybatis配置
│   └───target
├───eyecool-biapwp-thirdparty-video 第三方视频服务模块
│   ├───src/main
│   │       ├───java/cn/eyecool/video
│   │       │      ├───service 服务及实现
│   │       │      └───util  工具类
│   │       └───resources/mapper    mybatis配置│   │               
│   └───target
├───eyecool-biapwp-tools  工具模块
│   ├───src/main
│   │       ├───java/cn/eyecool/tool
│   │       │               ├───controller  功能接口
│   │       │               ├───domain  工具对象
│   │       │               ├───mapper  mybatis接口
│   │       │               └───service 服务及实现
│   │       └───resources/mapper    mybatis配置
│   └───target
├───eyecool-biapwp-tradelog 交易日志模块
│   ├───src/main
│   │       ├───java/cn/eyecool/tradelog
│   │       │               ├───constant  常量
│   │       │               ├───controller  功能接口
│   │       │               ├───domain  交易对象
│   │       │               ├───mapper  mybatis接口
│   │       │               ├───param 交易参数对象
│   │       │               ├───service 服务及实现
│   │       │               ├───task  定时任务
│   │       │               └───vo  数据对象
│   │       └───resources/mapper    mybatis配置
│   └───target
├───eyecool-biapwp-visitor  访客模块
│   ├───src/main
│   │       ├───java/cn/eyecool/visitor
│   │       │               ├───constant  常量
│   │       │               ├───controller  功能接口
│   │       │               ├───dto 数据传输对象
│   │       │               ├───mapper  mybatis接口
│   │       │               ├───service 服务及实现
│   │       │               ├───utils 工具类
│   │       │               └───vo  数据对象
│   │       └───resources/mapper    mybatis配置
│   └───target
├───eyecool-biapwp-webtrade 网络ABIS功能调用模块
│   ├───src/main
│   │       └───java/cn/eyecool/webtrade/controller 功能接口
│   └───target
├───pdm 数据库设计
├───proto proto3原型
└───sql 数据库脚本
            

```

## FAQ

### 访客功能注意点
1.向访客发送短信，短信信息较长，且带有url连接，添加了相关短信模板，但是发送短信到接收会有很长的延迟（20分钟以上），
短信状态是提交成功、发送中，要等运营商审核，要向相应的短信云服务平台客服反馈，让他帮忙让移动联通电信那边给处理一下，客服给处理好后，
时间能从20多分钟降低到1分钟。但是还是存在不稳定的情况，有时也会审核未通过。

### 兼容说明

1.  兼容非前后端分离版本，请注意进行数据库比对，进行表结构微调和非分离版本功能代码微调。
2.  表sys_user(非分离->分离)：login_name => user_name，user_name => nick_name。
3.  表sys_logininfo(非分离->分离)：login_name => user_name。
4.  表sys_menu(非分离->分离)： + [path、component、is_frame、is_cache、status]；- [url、target]。
5.  user和logininfo表更新字段名并修改非分离版本对应代码。
6.  menu表新增字段但是不删除旧的字段，确保新老字段全量存在。
7.  其他表根据数据库比对情况自行调整（例如gen_table可能相同也可能不同）,字段长度不同可以不做修改。

## 相关链接

- [下载地址](http://192.168.60.66/library/fbc40c57-39e4-4902-96db-6cc0ded506ec/%E4%BA%A7%E5%93%81%E5%8F%8A%E9%A1%B9%E7%9B%AE%E7%89%88%E6%9C%AC%E8%BF%87%E7%A8%8B%E6%B8%85%E5%8D%95/%202019012805_Saas%E5%B9%B3%E5%8F%B0)
- [BUG登记](http://192.168.0.68/zentao)
- [需求地址](http://192.168.0.68/zentao/index.php?m=bug&f=browse&productID=218)

## 支持


支持人员:

- 开发： [马文军](mawenjun@eyecool.cn) [孙华禹](sunhuayu@eyecool.cn) [何振东](hezhendong@eyecool.cn)




