# 概述
## 1.1 目的
目前使用的TCP请求，都是未经封装的，很容易出现一处错误需要在多处重复修改，不仅降低了工作效率，还会降低代码的简洁度，该lib服务将TCP请求的主要业务代码封装，开放接口供前端调用，如有修改，前端仅需在配置文件中修改依赖的版本号即可

## 1.2 主要内容
### 1.2.1 基本思路
考虑到项目中需要建立不同ip的socket，所以需要支持修改ip，进而需要建立管理socket的工具类，负责socket的连接，重连，数据的发送与数据的解析，一个socket对应一个工具类，让service后台管理这些工具类
### 1.2.2 使用说明
应用启动时，建立需要的socket

![](https://spark-docs.oss-cn-hangzhou.aliyuncs.com/docs/QQ截图20190213141931.png)

需要发送数据的界面，获取service对象，绑定服务

![](https://spark-docs.oss-cn-hangzhou.aliyuncs.com/docs/QQ截图20190213142340.png)

创建回调，发送数据

![](https://spark-docs.oss-cn-hangzhou.aliyuncs.com/docs/QQ截图20190213142824.png)

关闭界面时，解绑service
