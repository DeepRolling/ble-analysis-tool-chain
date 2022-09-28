
近期接到一个需求，抓取1000次蓝牙程序与智能硬件程序之间的交互数据，用于分析蓝牙通讯(gatt)过程中蓝牙协议栈的每个步骤的耗时。

由于蓝牙外设没有网络连接功能且不能上报数据至服务器，亦或者外设中没有关于蓝牙协议栈中某个具体事件的回调，总之我们不能通过蓝牙
外设去获取通讯的数据，所以我们需要用到nrf52dk nrf52832 4.1.0这一款由nordic半导体公司出品的蓝牙协议嗅探器（下文统称sniffer)
去抓取具体通讯过程中的协议。

每次的交互数据包含了各个时间节点，粗略来说各个时间节点如下:
1. app中开始扫描蓝牙外设
2. app找到蓝牙外设
3. app开始连接蓝牙外设
4. app连接蓝牙外设成功
5. app开始发现服务(discover service)
6. ap发现服务成功
7. app开始发送数据
8. app收到外设设备的回复

在以上步骤中除1、2步之外，其余的步骤除了在android framework中有对应的callback之外，使用sniffer也可抓取由packet header
标识的对应数据包，两者相减便可以算出从android执行具体步骤到sniffer嗅探到数据包的时间，如耗时过长则应针对具体步骤代码进行排查
以发现具体耗时的步骤，从而减少耗时。

也就是说2-8步中的每个蓝牙时间点在实际抓取过程中可抓取到两份数据（android端记录的数据，sniffer端记录的数据）

在获知要抓取的数据，经过技术调研，最终设计如下：

```text
├── custom-parser
│   ├── dist
│   │   └── index.js
│   ├── package.json
│   ├── src
│   │   └── index.ts
│   ├── tsconfig.json
│   └── yarn.lock
├── custom-sniffer-and-windows-driver
│   ├── LICENSE.txt
│   ├── Profile_nRF_Sniffer_Bluetooth_LE(folder)
│   │   ├── ...
│   ├── doc(folder)
│   │   ├── ...
│   ├── driver(folder)
│   │   ├── ...
│   ├── extcap(folder)
│   │   ├── SnifferAPI(folder)
│   │   ├── nrf_sniffer_ble.py (important file)
│   │   ├── ...
│   ├── hex(folder)
│   │   ├── ...
│   └── release_notes.txt
├── ...
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── example
    │   │           └── timerecordcollector
    │   │               ├── TimeRecordCollectorApplication.java
    │   │               ├── autoRunner
    │   │               │   ├── AutomationExecuteFailReason.java
    │   │               │   ├── AutomationExecutionCallback.java
    │   │               │   ├── AutomationRunner.java
    │   │               │   └── ParserResult.java
    │   │               ├── bean
    │   │               │   ├── ApplicationSideData.java
    │   │               │   ├── BleCommunicationSummary.java
    │   │               │   └── BleTimeRecord.java
    │   │               ├── controller
    │   │               │   └── TimeRecordHandler.java
    │   │               └── dispatcher
    │   │                   ├── BleCommunicationXmlWriter.java
    │   │                   └── MainDispatcher.java
    │   └── resources
    │       └── application.properties
    └── test(folder)
```
>File tree中一些不重要的文件夹以及其中的文件使用了 **_(folder)_** 作为标识，如 **custom-sniffer-and-windows-driver** 
下的文件大部分是在windows中安装sniffer(v4.1.0)需要的驱动以及wireshark的配置文件等。
> 
> 一些重要的文件后面加上了 **_(important file)_** 作为标识


##架构图

![Ble analysis system diagram](http://www.deepcode.site:9000/blog/ble-analysis-system-diagram-light.png)

##实现细节

> 本项目不含androidTest的代码，androidTest借用instrumentation test去实现了模拟用户操作手机的流程
> ，并将每个ble关键的时间点通过http请求发送给restful service

核心调度器为Dispatcher,负责控制Automation Runner去运行脚本，并负责异常处理、暴漏传输数据接口并最终汇总输出为xlsm文件的功能

Automation Runner 通过 java child process 的方式去控制windows运行命令去获取数据并汇总至Dispatcher。

如Automation Runner 运行过程中产生了异常，Dispatcher负责根据异常的种类去break程序或者继续进行下一次loop。

> **“Talk is cheap. Show me the code.”**    ― Linus Torvalds
> 
> **“Or you can read the diagram above bro.”**    ― me

##技术选型

#### _怎么启动Wireshark_
1. 使用lua编写脚本去控制wireshark软件抓取数据包
2. 使用wireshark安装时自带的command line然后使用java child process执行命令启动Wireshark客户端

最后选了第二种方案，因为第一种方案实现成本过高，需要掌握编程语言以及脚本编写规则

#### _Wireshark抓取数据包抓取不到的问题_

在启动Wireshark之后发现嗅探不到ble通讯的过程，最后定位(通过抓取wireshark传给plugin中的指令,如开始扫描、进入监听模式、跟随设备等）
为sniffer需要进入follow device mode才能嗅探到指定外设设备相关的数据包。

1. 使用windows平台上的ui自动化工具在Wireshark启动之后模拟点击跟随设备按钮（Wireshark没有提供command line去操作相关功能）
2. 修改plugin的python脚本在Wireshark启动之后(fixed 2 seconds)去发送伪造指令去控制sniffer进入follow device mode

see code **custom-sniffer-and-windows-driver/extcap/nrf_sniffer_ble.py:658**

#### _如何在得知androidTest运行完毕之后关闭wireshark抓取进程_

Wireshark command line没有提供关闭当前运行的wireshark进程的功能

1. 使用windows平台上的ui自动化工具点击关闭按钮
2. 使用windows command line去关闭所有wireshark.exe字样的进程，并监听running process的 IO message.

#### _Wireshark获取到的数据包解析问题_

Wireshark最后抓取的数据包是pcap格式，需要从pcap中取得相应的数据，但是java平台还有node.js平台都没有
比较完善的pcap中ble协议的解析库，python我不太熟～

1. 使用windows平台上的ui自动化工具将pcap在Wireshark中打开并自动点击导出按钮，导出为plain txt之后用java去解析数据
2. 使用tshark命令行工具将pcap文件直接转为json并自定义解析器(node.js方案)去解析

最后采用了第二种方案并通过java child process调用node程序针对tshark输出的json文件进行了解析，并输出解析后的json数据在相同目录。

#### _powershell 写入文件bom(byte order mark)的问题_

Powershell 5.x 写入文件一直带着bom，且没有参数控制是否添加bom

所以在parser解析的时候通过代码去将bom移除，否则会引发无法parse json的问题
```js
const rawData: string = fs.readFileSync('.\\..\\temp\\ble_data.json', 'utf-8');

//remove the stupid bom
const frames: EachBleFrame[] = JSON.parse(rawData.replace(/^\uFEFF/, ''));
```

Code Happy ~
