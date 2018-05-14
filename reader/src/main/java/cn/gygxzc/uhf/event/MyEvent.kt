package cn.gygxzc.uhf.event

/**
 * @author niantuo
 * @createdTime 2018/5/12 13:40
 * 该模块的事件注册
 * 这个是事件，回调事件
 * 考虑到向react 注册常量
 */
enum class MyEvent(val desc: String, val code: Int) {

    SimpleBleMgr("蓝牙事件", 1),

    BLE_NONE("蓝牙未连接", 0),
    CONNECTED("蓝牙已连接", 2),
    CONNECTED_FAILED("蓝牙连接失败", 3),
    CONNECT_START("开始蓝牙连接", 4),
    CLOSED("蓝牙已关闭", 5),
    SCAN_START("开始扫描蓝牙设备", 6),
    SCAN_END("蓝牙扫描结束", 7),
    BLE_SETTING_RESULT("系统设置返回", 8),
    BLE_DEVICE_FOUND("发现蓝牙", 9),


    RFID_EVENT("RFID相关事件", 20),
    RFID_INVENTORY_EPC("扫描到电子标签", 21),


    ;


}