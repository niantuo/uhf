package cn.gygxzc.uhf.event
/**
 * @author niantuo
 * @createdTime 2018/5/12 14:21
 * 错误码
 *
 */
enum class ErrorCode(val code: Int, val desc: String) {


    ERROR_NONE(0, "操作成功"),

    ERROR_IS_SCANNING(1, "正在扫描中"),
    ERROR_SCAN_ERROR(2, "蓝牙扫描错误"),
    ERROR_SCAN_END(3, "扫描完成"),

    ERROR_NO_FOUND_BLE(10, "没有找到匹配的蓝牙设备"),
    ERROR_CONNECT_FAILED(11, "蓝牙连接失败"),

    ERROR_BLE_UN_CONNECTED(20, "蓝牙未连接"),

    ERROR_BLE_NO_DEVICE(21, "没有目标蓝牙"),
    ERROR_BLE_NO_CONNECTED(22, "蓝牙未连接"),
    ERROR_BLE_DISABLE(23,"蓝牙不可用"),

    ;

}