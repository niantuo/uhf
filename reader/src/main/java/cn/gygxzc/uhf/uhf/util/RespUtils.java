package cn.gygxzc.uhf.uhf.util;

public class RespUtils {

    /**
     * 感觉自己基础好差，不知道byte + 是什么概念，转换成数字再加吗？
     * @param data  bytes
     * @return byte
     */
    public static byte checkSum(byte[] data) {
        byte crc = 0x00;
        // 从指令类型累加到参数最后一位
        for (int i = 1; i < data.length - 2; i++) {
            crc += data[i];
        }
        return crc;
    }
}
