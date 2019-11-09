# BMSProtocolAnalysis
Java application program based on socket and Modbus Protocol
# 基于scoket和modbus协议通信
本应用程序使用的是java语言，基于modbus协议通信，实现对客户电池设备的基本情况进行监控，包括电压、温度、内阻、电量、错误信息等
# 项目结构


  * dic------commonUtil  //通用工具
  * -----------ByteTransfer   //字节转换，包括四子节转浮点数，两字节转浮点数，十六进制字符串转字节数组
  * -----------Crc16Util      //Crc校验与生成
  * -----------PropertiesLoadUtil     //配置文件加载与处理
  * dic------dao   //数据库连接
  * -----------BMSDaoJdbcImpl      //数据库连接处理收到的数据实现类
  * dic-----------impl   //数据库接口与异常处理
  * --------------------DaoException      //数据库连接异常抛出与处理
  * --------------------dbBaseUtil      //数据库sql语句与执行
  * --------------------IBMSDao      //接口
  * dic------dbutil   //数据库连接池设置
  * ---------------DBPoolConnection      //数据连接池
  * dic------equipments   //设备
  * dic---------------ups     //ups设备
  * ------------------------Ups   //ups设备
  * ------------------------UpsInstruction   //ups指令
  * ------------------------UpsSendInstruction   //ups指令加载
  * ------------------------UpsSendInstructionType   //指令类型接口
  * dic---------------BMS   //BMS设备
  * ------------------------BMS      //BMS设备
  * ------------------EquipmentsFactory      //设备初始化工厂
  * ------------------IEquipment     //设备接口
  * ------------------IEquipmentInstruction      //指令接口
  * ------------------IEquipmentInsType     //指令类型接口
  * dic------log   //日志
  * dic------resources   //配置文件
  * ---------------bmsInstructions
  * ---------------mysql
  * ---------------pollingTime
  * ---------------socket
  * ---------------upsInstructions
  * dic------server   //主要服务
  * ----------------InstructionQueue      //指令队列
  * ----------------PolllingThread      //轮询线程
  * ----------------ReceiveAndSendData     //数据接受与发送
  * ----------------SenderAndReceiver      //发送接收线程
  * ----------------ServerMain      //服务器，监听端口，与设备建立连接
  * ---------log4j      // 日志记录
  * dic------resources   //配置文件
