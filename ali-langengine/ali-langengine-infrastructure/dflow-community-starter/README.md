dflow-community-starter提供了
dflow的扩展中 基于redis和RocketMQ的实现。以便核心代码开箱即用。
可以参考该工程自己实现。

使用说明：

properties文件中需配置好
app.name= your-app-name
com.alibaba.dflow.metaq-topic= 自收自发的TOPICID
com.alibaba.dflow.metaq-cid= 自收自发的TOPIC CID

另外环境中要有JedisPool

