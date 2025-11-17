# 背景与定位

技术上，我们有非常多富有挑战性的处理任务需要比较长的步骤和时间周期才能完成。尤其是在深入产业的进程中，这些工程工作越来越普遍。 比如通过一系列算法来完成一个复杂数据处理工作。

![DFlow.png](https://ata2-img.oss-cn-zhangjiakou.aliyuncs.com/f0670aa5e0dd8e708f7c74fc0dafd440.png)

最简单的做法是一个超长的同步任务。但是一个请求过长的线程，其性能和稳定性是难以保障的，RPC本身也无法支持过长的请求，导致节点任务需要是异步的，也会串不起来。

所以更常见的实践是用消息订阅一个一个节点串联起来。这种分布式中间件的使用破坏了本地编程语言和研发体系的一致性，除了引入分布式系统的新问题（如局部失败的重试等处理）以外，还带来了一系列的退化：开发调试不方便，逻辑关系不清晰，重构与复用困难。



市面上的流程引擎是解决这些实际问题的一些工作。但是它们是在更高的上层建筑的层面构建的系统。 将业务逻辑放到了更多层面的系统去组织。 他们解决了业务代码运行时的各种问题，但是实施起来不是特别的方便（需要单独系统的架设，中间的流程串联需要跳转不同的工具和代码位置），所以不到特别重要的业务场景要把流程单独管理，杀鸡懒得用牛刀。

DFlow的理想是进一步简化异步流程编排，去中心化的库设计，编排逻辑和节点业务代码在一个地方不破坏业务逻辑流畅性；思路是in language的 [monad](https://www.evernote.com/shard/s279/client/snv?noteGuid=a3b59986-0488-4914-bbbd-633161565c92&noteKey=f69ab4eda37ab94ba71528a102ce5472&sn=https%3A%2F%2Fwww.evernote.com%2Fshard%2Fs279%2Fsh%2Fa3b59986-0488-4914-bbbd-633161565c92%2Ff69ab4eda37ab94ba71528a102ce5472&title=Functors%252C%2BApplicatives%252C%2BAnd%2BMonads%2BIn%2BPictures%2B-%2Badit.io)  库，新构建了 分布式流程范畴 的类型，使得业务编写流畅，无需刻意关心流程语义。类似List处理写代码只需关注单个数据处理，用DFlow做异步编排逻辑只需关心每步做些什么，在原生代码中书写流程串联，剩下的交给库的机制解决分布式系统中引入的和业务逻辑无关的问题。 其设计哲学是最大化地降低学习和使用难度的曲线，对于常见问题可以在需要时再慢慢感知和理解。

具体实现下来DFlow，和传统流程引擎相比，最大的优势就是和原生代码互通的便捷，代码可读性和复用度高，开发调试能最大程度地利用原生生态的各类设施（一个很amazing的事情，你可以在DDD的domain层用面向对象的方式组织编写复杂流程，在service复用对象的局部流程，并单元测试），无需额外学习便可理解业务逻辑。 和Reactive Stream相比，将其异步化的本地思想放到了分布式编排的慢任务域。 和数据流引擎相比，面向逻辑流程的而不是函数数据变换的类型 使得其有 更强大舒适的逻辑编写&调试能力，其调度部署一体的去中心化架构依赖轻，与业务环境结合也更加方便。


# 核心亮点
![image.png](https://ata2-img.oss-cn-zhangjiakou.aliyuncs.com/03c574b50a64518c63f8cd97aa9bf18c.png)

- 开发友好性：
  **部署便利，资源自备，系统无依赖**：去中心化的调度器，无需单独为基础设施部署新的异构系统。资源自备，系统无依赖稳定性不依赖额外的外部系统。

  **原生Java业务编码体验**：理论最完备的扩展和适配性。

  **流程片段可复用**：流程片段可以是一个Java函数，可复用&集中维护，组装完整流程而互不干扰。

  **核心的流程串联等功能**： 具备串联节点的各种基本能力。

  **流程逻辑可单机单元测试回归**: 同一段代码，基于Monad的基础设施使得一行代码切换工作方式，从分布式异步变为本地执行，使得复杂流程代码有可本地化调试的机会。

- 分布式问题处理：

      **支持异常及重试处理**：可以应对流程节点的各类异常
      
      **支持全链路单机代码Debug**：可以在一个机器上Debug分布式流程
      
      **中间节点回放Debug**：可以不用走全流程回放局部逻辑，加快debug和代码修复。
      
      **全链路运行周期及中间数据查看**：可以无需额外写代码自动局部运行状况观测能力。

- 对于DFlow的了解程度和适合的任务依次如下：
  1.仅仅了解语法能跑起来hello world： 无需实时在线的长周期重任务。
  2.了解代码初始化的生命周期原理和在线更新的Best Practice，问题排查： 极限压力不大的后台生产型实时在线的天级别长周期重任务，寄生于资源足够的宿主应用。
  3.深入理解adapter对性能的影响，了解节点流程调优：用满机器资源的极限专用集群。


# 竞品对比


以阿里内的星云NebulaX（开源的Activiti，Flowable，以及https://segmentfault.com/a/1190000038796413 之流的自研小引擎）作为BPMN流程引擎的代表， 还有社区的Spring Cloud Data Flow https://dataflow.spring.io/;  最后是以及希望往业务流方向发展的数据流引擎  的代表Flink。

就现状来说：

|项|星云NebulaX/前身Drama|临时手撸|DFlow|Spring Cloud Data Flow|Flink|
| --- | --- | --- | --- | ---|---|
|流程规范| BPMN2.0通用 | 自己写 | 流式编程的Java代码 |Unix管道&图形化DSL| 流式编程的Java代码
|流程引擎|有向图| - | - | - |-|
|运行状态|有状态/无状态|有状态/无状态|有状态/无状态| - | -
|节点类型|人工，自动，同步，异步，事件，定时，子流程，汇聚，分组节点，异常，决策|任意需要的|map（自动）,flatMap（子流程）,zip（汇聚全部）,any（汇聚任意一个）,just（初始）,call（外部触发）| - | 聚合，过滤，flatten打平
|决策规则|QLExpress、JUEL、NASHORN|Java代码|Java代码| groovy 配置节点| Java代码
|动态部署|支持|不支持，基于应用容器体系变更|不支持，基于应用容器体系变更|支持| - |
|灰度发布回滚|支持|不支持，比较困难|支持，需注意执行计划，同应用变更体系|-| -|
|跨应用流程|支持|各做各的|各做各的可串联，增加remote节点后可串联|-| -|
|流程配置|完备Web管理系统|写代码|写本地可读代码|完备Web管理系统&DSL代码| 本地代码|
|使用方式|集中式配置管理+本地嵌入式运行|本地嵌入式运行|本地嵌入式运行|集中式服务器| 集群|
|存储扩展|支持自定义存储|自定义存储|自定义存储|自定义存储|中心化系统|
|高级特性|分租户、全球化管理、流程版本、重试（手动/自动/同步/异步）、发布（跨环境/灰度）、调试| 无 |-|-|数据MapReduce等操作
|存在年限|1年多Drama进化为NebulaX|-|3年间断打磨|3年从Spring Cloud Task开始| 最近开发发力业务编排
|社区规模|菜鸟中间件团队|-|虬枝0.x个人|Spring开源社区| 公司
|总结|性能高，扩展性强，高级特性多，配套完善，支持力度大|灵活，开发速度快，无法应对复杂场景|极简核心引擎，简单使用便捷，配套稍弱，深度使用暂时不直观|社区和几大云vendor支持，功能不强 | 基于数据流编排，对于简单大数据固定函数关系处理的场景比较合适|

由于理念上的差异，传统流程引擎和InLanguage的库，最大的差别在于传统流程引擎使用异构的DSL控制执行流，很难建设相对发展多年的开发语言来说相当的完备性（比如Java中各种设计模式对于复杂代码的组织方式）和配套研发体系（IDE，debug工具链，单元测试设施）。 而InLanguage库最大的问题，则是以哪种方式用上这些设施，让用户在那个层面去理解对分布式的感知，并且用户不容易在分布式和本地流程的变化中犯错，既要融合、又要可控，否则要么是无法实现业务流程编排时无需时刻关注非业务特性，要么过于死板无法和原生代码逻辑融合。

和Flink相比，他们出发点的不同（要解决海量数据，单数据可能简单，需要reduce之类的关联计算；不同于DFlow和流程引擎为了解决单个复杂任务，重数据的问题）带来了本质上的区别：他们是基于数据流的编排引擎，抽象的节点代表的是数据，编排的是对数据的处理逻辑；而DFlow抽象的是流程，组织的是流程链路。 体现在功能上，就是基于数据流的引擎更关注数据变换的扩展，处理流程是固定的函数式思想，外部交互能力弱。原生没有像逻辑流程一样，有任务，状态，上下文，分支流，异常流这样的面向过程的处理概念，对任务级调试的能力也不容易原生具备，代码组织没有那么灵活；不过更大的区别，在于Flink为了大数据处理的高效，是中心化重系统的架构，需要把自定义逻辑提交一个jar到远程去，重业务代码是无法简单提交到远程进行的，也无法接受中间的请求。



总的来说，在现在阶段DFlow实现了比较核心的能力，也经受了一些线上比较重要环境（模型处理链路，各种算法处理链路,一些AI Agent）的稳定性考验。


# 使用方法
## 1. 引入与Hello World
### 1.1 引入与初始化
1.1.1. 首先引入dflow包

    <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>dflow</artifactId>
            <version>0.1.13</version>
        </dependency>


DFlow借助消息中间件完成节点之间的通知触发串联。通过kv存储保存任务信息和全局初始化状态信息，通过rpc来兼容代码初始化前的请求转发&调试功能的暴露。因此要设置 消息，存储，RPC三大中间件。

核心的几个工作：
1.设置好DFlow.setRequestResender 转发
2.注册配置好消息 DFlow.setSubscribableChannel
3.准备好基于redis的ContextStoreInterface， DFlow.setStoreage
4.如果要用DFlow.delay。 需实现一个 DFlowDelayManager，在分布式环境中在指定时间触发（本地实现是本地Sleep）。

这些实现有dflow-commuinity-stater提供了基于社区中间件实现的版本。

1.1.3. 然后就可以写代码了 

	import java.util.UUID;
	
	import javax.annotation.PostConstruct;
	
	import com.alibaba.dflow.DFlow;
	import com.alibaba.dflow.InitEntry.Entry;
	import com.alibaba.dflow.PipeLineInfo;
	import com.alibaba.dflow.internal.DFlowConstructionException;

	@DependsOn("DFlow") //starter 时需要它
	public class SpringComponentBean{
	    //入口函数
	    private Entry entry;
	    @PostConstruct
	    public void init() throws DFlowConstructionException {
	     
	
	        //此处构建串联的DFlow节点，本例中一个Call节点和一个Map节点
	        DFlow<String> n = DFlow.fromCall("Start").id("firstNode")
	            .map((ctx,param)-> {
	                System.out.println( "Hello" + param);
	                ctx.put("Result","Hello" + param);
	                return "";
	            } ).id("lastNode");
	        //此处初始化流程
	        PipeLineInfo info = n.init("流程v1");
	        //此处取出流程中的起始DFlow节点对应的Entry供运行时调用。
	        this.entry = info.getEntry("Start");
	        startService("自检任务");
	    }
	
	    public void  startService(String param) throws Exception {
	        String traceId = UUID.randomUUID().toString();
	        //此处触发Entry驱动流程运行一个任务。
	        this.entry.call(param,traceId);
	    }
	
	    public static void main(String[] args) throws Exception {
                //此处初始化整个DFlow，简单起见本地mock所有中间件。如正常的starter，靠DependsOn不需要这一行
	        DFlow.globalInitForTest();
	        SpringComponentBean b = new SpringComponentBean();
	        b.init();
	        b.startService("World");
	        b.startService("World2");
	
	    }
	}

除了这个例子， 可以参考test目录下 DFlowOperatorTest中的各操作符，更简单的是利用directRun的封装来直接执行，在没有中间Entry的case时，这样可以简单地统一定义和执行流程的代码。 

## 2.基本概念
### 2.1 名词解释
- **DFlow节点**：DFlow节点是 一份数据及其处理过程 在分布式异步流程中的代码表达。com.alibaba.dflow.DFlow

- **DFlow算子**：DFlow算子是创建各种类型的DFlow节点的函数。
- DFlow.just(T) 算子 将一个单机内存普通数据T变换到DFlow节点，使得一个普通数据能在流程编排域中开始流转。运行时将直接把T继续向后流转。
    - DFlow.flatMap(this, fn) 算子串联起当前DFlow节点和下一个任意DFlow节点， 接受fn函数（是一个接受一个处理上个DFlow节点流转下来的数据，并返回新的DFlow节点的函数），其含义是创建一个DFlow节点，这个节点将当在运行时把当前DFlow节点处理完毕的数据传递给fn函数，fn处理完毕后的DFlow节点也会开始触发，并将其结果继续向后一个DFlow节点流转。
    - DFlow.map(this,fn2)  算子是常用的简化的两个任务串联处理，接受fn2函数（是一个接受一个处理上个DFlow节点流转下来的数据，并返回新的数据的函数），其含义是创建一个DFlow节点，这个节点将当在运行时把当前DFlow节点处理完毕的数据传递给fn2函数，并把处理完毕后的数据继续向后一个DFlow节点流转。      逻辑等价于（DFlow.flatMap(this,(x)->{ DFlow.just(fn(x))})）
    - DFlow.fromCall(Entryname)算子 定义一个有Entry名字的DFlow节点。整个流程创建以后，可根据Entry名字找到这个流程对应的Entry。 在运行时，其在接受来自Entry的外部输入时，将外部输入数据向下一个DFlow节点传递。

- **流程**：流程是一些DFlow节点组装在一起并初始化以后，编码时确定的所有可能的运行时流程拓扑集合的抽象描述。 （对于一个编码时确定的流程实例，由于DFlow节点的动态性，不同的输入可能有不同的流转链路）。 PipeLineInfo
- **任务**：一个任务是流程的一次具体的执行过程。具有traceId。
- **Entry**: Entry 是一个流程对外部输入的响应接口。一个流程创建之后，运行时须有外部输入来驱动每一次任务的运行。 get from PipeLineInfo
- **DFlow功能**：DFlow功能是一组辅助函数，用于辅助分布式流程的非业务问题处理。


### 2.2 工作原理



DFlow的工作原理:

![image.png](https://ata2-img.oss-cn-zhangjiakou.aliyuncs.com/0975e065a2626aaa30e88c7ded76504d.png)

首先是用户通过DFlow算子把各种DFlow节点创建和串联起来，表达其业务逻辑。

然后通过init创建流程。init的时候，会在本地全局内存中记录流程节点step,这样，在触发实际任务之时，每个step都会通过消息adapter在任意一台机器上执行一个流程节点。流程节点是DFlow节点在具体一个流程中的实例，其key为当前流转链路剩下的DFlow节点名串联在一起

    （一份代码表达的DFlow节点在不同流程中是不同的流程节点，同一个流程的不同的任务会复用流程节点）。流程节点的key在代码中用id()函数来命名，若DFlow.setStrictMode(false); 则DFlow会默认用com.alibaba.dflow包外的第一个调用栈代码行数作为key)

value是具体一个DFlow节点的内存实例，因此会根据一个节点名刷新实例，须保证不同实例的逻辑一致性，不可有流程相关局部闭包

     （DFlow会做内置校验，报错使得首次运行不会通过；对于特殊场景，开放SafeObject使得用户可以自定义多流程一致的闭包数据；对于常见的无问题场景（bean内数据，仅做Warning）；）。

        注意对于flatMap内的节点，是动态初始化的，这会带来一个新的问题：某些机器没有动态初始化好但是接到了处理需求（解法是转发到已经初始化好的机器；另外，通过同级计数，在本机运行全部流程，快速初始化一台机器上的整套代码）

任务的触发通常从外部任务开始，因此一般以DFlow.fromCall作为第一个入口。init之后可以从PipelineInfo中获取到这个流程中所有DFlowCall节点的Entry。 在外部触发之时用这个Entry来驱动流程的进行。

更详细的原理的介绍，附件ppt中有部分描述。

## 3. 基本使用
### 3.1流程构建
#### 3.1.1 外部输入
DFlowCall节点在运行时通过Entry输入一个String参数并传递下。

    DFlow<String> node = DFlow.fromCall((contextStack)->{
              //此处trigger代码可选，发生在任务触发到已经初始化好的Call节点，等待触发之前。
              // 通常用于flatMap中串联外部链路时触发一个新流程。
              //（若直接在flatMap代码中进行外部串联，有一点点可能性首次DFlowCall节点还没有初始化完毕回调Entry的call就来了导致丢失一个请求。）
    },"EntryName").id("callNode");
通过fromCall算子创建一个节点，代表该节点对应的外部输入Entry最后需通过完整流程初始化以后，从PipeLineInfo中获取出来，以后通过Entry就可以触发这个节点的继续运行。注意DFlowCall节点在流程中部时，流程进行到会暂停直到收到来自Entry的调用。

id()方法用于唯一标识代码能力片段。若不指定，且在strictMode为false的情况下，其默认值为当前代码行数，如果是实时在线场景，会需要频繁要处理流程变化的情况。
#### 3.1.2 顺序流程
简单的顺序流程可以通过DFlowMap节点构建，通过自定义函数进行处理并变换数据往下处理。

      DFlow<Integer> mapnode = node.map(MyClass::CustomFunc,Integer.class).id("work")

最后的入参是表示map之后的返回类型，便于编译器理解类型推断，以及FastJson做持久化处理，默认是String类型，请不要使用非Pojo在节点间传递以免出现无法正确序列化反序列化的问题。
map算子是任何DFlow节点实例都有的类实例方法，他创建一个新的节点并和当前节点串联起来，返回串好的下一个节点，是的可以链式编码地一直构建下去。

	  //自定义函数的入参是任务上下文ContextStack和上一个节点的入参
	  Integer CustomFunc(ContextStack ctx, String param){
	      ctx.setLocal();
	      ctx.setMock();
	      ctx.put("taskData","11");
	      ctx.get("taskData");
	      throw new RetryException("抛此异常节点会基于消息adapter重试，其它异常会流程结束")
	      return Integer.parse(param);
	  }
##### ContextStack 任务上下文
自定义函数都可以获取任务上下文，在任务上下文中，可以存取传递整个任务粒度内的数据。
一些高级功能也通过这个对象来设置（譬如setLocal之后，该任务的所有的请求都会重新路由到当前机器，便于在线调试长流程中的问题）。
#### 3.1.3 流程组合
更为广泛的流程组合都是通过DFlowFlatMap节点进行组织。

     DFlow<Integer> flatmapnode = mapnode.flatMap((ctx,p)->{
        if("world".equals(p)){
            return DFlow.just("Test");
        }else{
            return subFunctionReturnDFlow();
        }
     }.id("flatMap")

自定义函数除了返回值是DFlow类型外，其他功能和DFlowMap的相同。对于分支流程可以直接在代码中ifelse。
有趣的部分就在返回值类型。以下操作符可以从凭空初始一个没有上级节点DFlow。

- DFlow.just(Value)： flatMap中返回just效果等同与map。 just节点在flatMap中被触发时直接将Value向下传递，是flatMap中的常见起始触发代码。由于其功能没有特异性，即使在strictMode也不要求指定id。
- DFlow.fromCall: flatMap中的DFlowCall使得外部调用可以从中间暂停和重新驱动流程的任务进行。对应可选的trigger用于在任务进行到流程时触发外部任务，以便Entry有机会被触发。  若入参是多个EntryName，创建的是DFlowMultiCall，会当所有Entry都调用执行到以后统一merge结果往下传递。
- DFlow.zip: flatMap中的zip会并发地触发每个子流程，并在处理完毕后统一返回。可选的trigger用于触发非DFlowJust 的外部子流程。底层是用DFlowMultiCall组装的。
- DFlow.any: flatMap中的zip会并发地触发每个子流程，并在任意一个节点完毕后立即返回。可选的trigger用于触发非DFlowJust 的外部子流程。底层是用DFlowMultiCall组装的。
- DFlow.delay: flatMap中的delay会触发一个延迟节点，延迟时间由参数决定。需实现setDFlowDelayManager，在分布式环境中在指定时间触发（本地实现是本地Sleep）。

#### 3.1.4 异常处理
在业务代码中通过 throw new RetryException("抛此异常节点会基于消息adapter重试，其它异常会流程结束")

任何一个节点上都有 OnErrorReturn函数可以设置ErrorHandler，当流程中发生异常时，会一直传递到有 ErrorHandler的节点处理完毕后继续运行 或终止流程到最后。

## 4. 运行与调试
## 5. 注意事项
### 5.1 编译问题
务必每行代码都测到再上线。 DFlowConstructionException是为了防止用户写出有风险的代码设置的保护性异常，流程初始化时报错，但是由于flatMap中的代码是动态执行的，所以部分的初始化错误会发生在运行时，在errorMsg中看到ConstructionException务必仔细看清问题，修复代码书写。 这些错误不会在启动后第二次调用出现，因此尤其要关注刚写的代码。
### 5.2 异常处理
- **稳定性异常**:对于流程中的异常，若是重启之类的系统异常，DFlow会自行基于消息adapter的机制重试，对于进程死亡之类的极端情况，一个节点有可能执行多次，业务代码需自己保障正常逻辑的执行幂等性。
- **业务异常**:业务异常一般会导致整个流程的终结，DFlow会一个个节点地跳过正常执行，直到有OnErrorReturn的DFlow节点进行业务异常处理，或是整个结束。 对于希望本节点重试的异常，业务代码可以Throw RetryException。
### 5.3 机器重启
在重启机器时，任务初始化并不会彻底完成（flatMap中的节点需首次执行过才能初始化）。这时刚重启机器没有执行过flatMap节点却接到了这个flatMap节点中间的节点的请求时，会找不到节点代码而转发给其它节点。
极端的情况，所有的机器都重启了，并且没有新的请求再进来（批量跑任务的场景）执行flatMap节点。会使得完全找不到节点而不断重试。

简单的问题解决是重启后再跑一个任务，之前堆积的任务就会找到一个节点跑下去。   
最佳实践是 **自检任务**：在应用初始化任务后，自动做上述动作：跑一段预热好所有可能的流程节点（一般来说只需要预热到耗时长的分支）。这样应用发布后无论是哪个流程，应用中都有ready的流程节点。

### 5.4 流程更新
**如果任务可以停机变更，这节可忽略。**还可以放心地开启strictMode为false的模式。这种模式下，甚至无需为每个节点设置id（DFlow默认会使用代码初始化的行数来唯一确定代码）。

主要是时刻保证在线的变更如何处理。
分布式流程编排更新一般有两种期待的结果，一种是老任务永远走老的逻辑，新任务永远走新的逻辑，任务代码不交叉，保证逻辑正确性；另一种是任务可以在中间走新老代码都可以。那么DFlow如何应对这两种情况呢？

#### 任务不交叉实践
先说不想串的情况：DFlow基于节点串联的全节点来认定一个流程，任务不会在不同的流程中串。 因此，若流程节点的数量或者节点id发生变更，两个流程不认为是同一个，那么之前正在执行中的任务有可能由于系统中不再有初始化完毕的流程节点而找不到相关流程节点，不断重试直到消息死信或代码回滚、初始化好旧的流程节点。

对此实践策略有如下几种：
- 1.**保留两个版本代码。对于打赏增改流程节点名的变更，建议复制流程编排的DFlow节点组织代码再创建v2版本（保障老流程的串联step不变并被初始化）。两个流程都要初始化。**
    -     参考5.3. 代码初始化并不是很trivial的工作，因为DFlow支持动态的流程创建，flatMap中的代码没有跑过之前是不会初始化的，任务执行到没有跑过的代码会寻找已经初始化过的机器，但是全都没有初始化过，就会找不到流程节点。对于没有用到flatMap的流程编排无需担心下面的措施，简单init两个即可。对于用到了flatMap的，也有两种实践：
    - 1. **入口处通过开关切换流程。代码全部发布之后，先不要切换流程，通过线上流量初始化好老链路。之后再把开关切换为新流程。**
    - 2.为流程构建自检任务，应用启动时触发预热好所有可能的流程节点（一般来说只需要预热到耗时长的分支）。这样应用发布后无论是哪个流程，应用中都有ready的流程节点。
- 2.发布的最后一批机器保留一段时间，并将入口Entry的请求全路由到前几批机器上开启新流程，直到老流程任务全部跑完再进行最后一批发布。 此策略需手动查看流程的进度注意发布。 更自动化的流程需和发布体系耦合起来。
#### 任务可交叉实践
对于可交叉的期待:老的任务也允许跑到新逻辑上来。
局部变化：若每个DFlow节点的id不变，那么其中的变更就是可交叉的。
链路分段：构建多段流程，流程之间通过外部Entry的方式，用新的任务串联。前一个流程的最后节点call后一个流程的Entry。 这样两个流程就没有耦合关系。可独立演进替换。
### 5.4 资源申请和观察
默认的存储8天历史记录。需看看redis空间是否足够。根据自己的资源调整参数。

DFlow虽然支持用户在一个集群中既做调度又做worker，但是阻塞性的任务不宜太多，否则影响占用了太多消息线程，可能会拖慢这台机器上所有的DFlow任务调度。 因为DFlow库直接同步接受消息adapter的消息处理，在消息线程中处理用户逻辑。

同时一个任务中的中间状态数据过大，也可能影响性能。

## 6. 高级使用

### 6.1 调试
####全链路查看
在当前版本，可以通过traceId来调用api获取整个任务执行状态，包括中间数据和流程全局变量。



以下列代码为例：这个代码构建了一个流程，包含了分支&外部触发&并发等特性。

             Entry entry = DFlow.fromCall("startTask").id("启动节点")
            .map((c, p) -> {
                System.out.print("1");
                c.put("a",1);
                c.put("b",2);
                return p;
            }).id("任务全局变量放到Context中")
                .flatMap(x-> {
                    System.out.println("2");
                    if("非分支流程".equals(x)){
                        return DFlow.just(x);
                    }

                    return DFlow.zip((ctx,returns)->{
                        //merge函数处理两个子链路返回值
                        return returns[0]+returns[1];
                        },
                        String.class,

                        Param.pack(DFlow.just("")
                        .map((c,p)->c.get("a")).id("分支链路1,id用于唯一确定一段代码，不可重复")),

                        Param.pack(DFlow.fromCall("Inner").id("分支链路2模拟从外部触发")
                        .map((c,p)->c.get("b")).id("分支链路2"),
                            //Trigger函数触发外部调用，c.getId()获得当前链路的traceId，从当前Inner中断继续
                            //对于中间Call节点的Entry，可以任意new或者从pipelineInfo中取，因为有traceId使得其不会串。
                            (c)->{DFlow.call(new Entry("Inner"),"",c.getId());
                        }
                        )
                    ).id("并发处理两个任务");
                }
            ).id("内部处理并发")
            .map((c, p) -> {
                System.out.println(p);
                c.put("assert","12".equals(p));
                return "";
            }).id("last")
            .init().getEntry("startTask");

运行

         //运行时触发
        DFlow.call(entry,"并发流程","ttt");

在结束后或者过程中查看流程状态 (DFlow.getStoreage().getContext("ttt"));结果如下：

	{
	/**********************************当前状态区***********************************/
		"errorMsg": "null",       //错误原因
		"finished": true,           //流程是否结束
		"global": {                   //contextstack中的全局变量在此，无注释的部分是业务字段
			"a": 1,
			"b": 2,
			"_FINISHED": true,       //流程是否结束
			"startId": "ttt",
			"assert": true,
			"_NEXTSTEP": "TERMINATED",     //下一个即将开始的流程节点，持久化数据用于校验，TERMINATED表示已结束
			"startId0": "ttt"
		},
		"iP": "30.13.121.20",  //当前执行的机器
		"id": "ttt",   //任务traceId
		"local": false,    //后续任务是否仅在一台机器上执行
		"mock": false,       //是否采用mock数据
		"name": "last-last->null",       //当前流程节点
		"nextStepId": "TERMINATED",     //下一个即将开始的流程节点，内存数据，实际下一步
		"noStorage": false,        //内部使用，当前节点完毕后不会持久化状态，用于单节点重放调试
		/**********************************每个节点状态***********************************/
		"stack": [{
			"_STATUS": "END",   //当前节点状态，BEGIN表示正在运行，ERROR表示错误
			"_NAME": "启动节点-启动节点->任务全局变量放到Context中-任务全局变量放到Context中->内部处理并发-内部处理并发->last-last->null",         //流程节点名，串联之后唯一表示
			"_IP": "30.13.121.20",  //该节点执行的机器
			"_DEBUGNAME": "启动节点",   //代码中用name指定的别名，未指定时用id
			"_STARTTIME": 1618286090040,       //该节点执行的开始时间
			"_RET": "分支流程",     //该节点执行完毕的返回值
			"COSTTIME": 38    //该节点耗时
		}, {
			"_STATUS": "END",
			"_NAME": "任务全局变量放到Context中-任务全局变量放到Context中->内部处理并发-内部处理并发->last-last->null",
			"_IP": "30.13.121.20",
			"_PARAM": "分支流程",
			"_DEBUGNAME": "任务全局变量放到Context中",
			"_STARTTIME": 1618286090123,
			"_RET": "分支流程",
			"COSTTIME": 1
		}, {
			"_STATUS": "PREPARE_SUBCALL",
			"_NAME": "内部处理并发-内部处理并发->last-last->null",
			"_IP": "30.13.121.20",
			"_PARAM": "分支流程",
			"_DEBUGNAME": "内部处理并发",
			"_STARTTIME": 1618286090125
		}, {
			"_STATUS": "END",
			"_NAME": "并发处理两个任务$2-并发处理两个任务$2->last-last->null",
			"_IP": "30.13.121.20",
			"_CHILDTASK": ["ttt_4-0", "ttt_4-1"],      //并发任务会启动若干子任务，可以重新用新的traceId查stack
			"_DEBUGNAME": "并发处理两个任务$2",
			"_STARTTIME": 1618286090141,
			"_RET": {
				"data": ["1", "2"]
			},
			"COSTTIME": 14
		}, {
			"_STATUS": "END",
			"_NAME": "innerzipm并发处理两个任务$2-innerzipm并发处理两个任务$2->last-last->null",
			"_IP": "30.13.121.20",
			"_PARAM": {
				"$ref": "$.stack[3]._RET"
			},
			"_DEBUGNAME": "innerzipm并发处理两个任务$2",
			"_STARTTIME": 1618286090161,
			"_RET": "12",
			"COSTTIME": 9
		}, {
			"_STATUS": "END",
			"_NAME": "last-last->null",
			"_IP": "30.13.121.20",
			"_PARAM": "12",
			"_DEBUGNAME": "last",
			"_STARTTIME": 1618286090172,
			"_RET": "",
			"COSTTIME": 1
		}]
	}



### 6.2 调试&维护
####运行时Debug
断点在自定义代码中， 可以ctx.setLocal()，或者在调用call之前，DFlow.setLocal()。使得任务此后都路由回当前机器。
####单步重试
DFlow.replayStep(String traceId,String stepName,boolean onlyThisStep)。  对一个已经执行完毕的流程，从某个中间节点，用当时的数据重放。 可以选择仅仅当前节点执行或剩下的节点也一起重新执行。
####查看堆积任务数
DFlow.getCount(step)， 查看流程节点运行中的任务数。
####手动结束任务
DFlow.addOmitTaskPattern(regex)，忽略单机正则表达式匹配的taskId继续处理。 用于结束某些不停retry又无法的结束的任务。 DFlow.queryPattern()， DFlow.removePattern() 配套维护接口。

### 6.3 AOP扩展点
DFlow.addHandler(StepHandler h) 可以在每一个自定义函数前后进行一定的处理逻辑



