package cn.gygxzc.uhf.uhf.cmd;

import java.util.Arrays;

import cn.gygxzc.uhf.uhf.util.Tools;

/**
 * 指令的一个厂，
 * 实际上是把所有的指令都放这里，几种管理
 */
public class CMDFactory {

    public final byte HEAD = (byte) 0xAA;
    public final byte END = (byte) 0x8E;
    public final byte RESPONSE_OK = 0x00; // 响应帧0K


    /**
     * 获取版本信息的指令
     */
    public byte[] firmware = {HEAD, (byte) 0x00, (byte) 0x03, (byte) 0x00,
            (byte) 0x01, (byte) 0x00, (byte) 0x04, END};


    public byte[] select = {HEAD, (byte) 0x00, (byte) 0x0C, (byte) 0x00,
            (byte) 0x13, (byte) 0x01, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x20, (byte) 0x60, (byte) 0x00,
            (byte) 0x01, (byte) 0x61, (byte) 0x05, (byte) 0xB8,
            (byte) 0x03, (byte) 0x48, (byte) 0x0C, (byte) 0xD0,
            (byte) 0x00, (byte) 0x03, (byte) 0xD1, (byte) 0x9E,
            (byte) 0x58, END};
    /**
     * 取消选择
     */
    public byte[] unselected = {HEAD, (byte) 0x00, (byte) 0x12, (byte) 0x00,
            (byte) 0x01, (byte) 0x01, (byte) 0x14, END};
    /**
     * 实时存盘
     */
    public byte[] inventoryRealTime = {HEAD, (byte) 0x00, (byte) 0x22, (byte) 0x00,
            (byte) 0x00, (byte) 0x22, END};

    /**
     * 多标签存盘
     */
    public byte[] inventoryMulti = {HEAD, (byte) 0x00, (byte) 0x27, (byte) 0x00,
            (byte) 0x03, (byte) 0x22, (byte) 0x27, (byte) 0x10,
            (byte) 0x83, END};

    public byte[] stopInventoryMylti = {HEAD, (byte) 0x00, (byte) 0x28, (byte) 0x00,
            (byte) 0x00, (byte) 0x28, END};

    /**
     * 读取数据，实际上这个只是原始的命令，还会对其进行处理
     */
    public byte[] readData = {HEAD, (byte) 0x00, (byte) 0x39, (byte) 0x00,
            (byte) 0x09, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x08, (byte) 0x4D, END};


    public byte[] sensitive = {HEAD, (byte) 0x00, (byte) 0xF0, (byte) 0x00,
            (byte) 0x04, (byte) 0x02, (byte) 0x06, (byte) 0x00,
            (byte) 0xA0, (byte) 0x9C, END};

    public byte[] outputPower = {HEAD, (byte) 0x00, (byte) 0xB6, (byte) 0x00
            , (byte) 0x02, (byte) 0x0A, (byte) 0x28
            , (byte) 0xEA, END};

    public byte[] getOutputPower = {HEAD, (byte) 0x00, (byte) 0xB7, (byte) 0x00,
            (byte) 0x00, (byte) 0xB7, END};


    public byte[] createReadCMD(int memBank, int start, int length, byte[] accessPwd) {
        int len = readData.length;
        byte[] bytes = new byte[len];
        System.arraycopy(readData, 0, bytes, 0, len);
        System.arraycopy(accessPwd, 0, bytes, 5, 4);
        bytes[9] = (byte) memBank;
        if (start <= 255) {
            bytes[10] = 0x00;
            bytes[11] = (byte) start;
        } else {
            int addrH = start / 256;
            int addrL = start % 256;
            bytes[10] = (byte) addrH;
            bytes[11] = (byte) addrL;
        }
        if (length <= 255) {
            bytes[12] = 0x00;
            bytes[13] = (byte) length;
        } else {
            int lengH = length / 256;
            int lengL = length % 256;
            bytes[12] = (byte) lengH;
            bytes[13] = (byte) lengL;
        }
        bytes[14] = Tools.checkSum(bytes);
        return bytes;
    }


    public byte[] createWriteCMD(byte[] password, int menBack, int startAddr, int dataLen,
                                 byte[] originData) {

        int readLen = dataLen - startAddr;
        byte[] data = new byte[readLen];
        Arrays.fill(data, (byte) 0x00);
        if (originData.length >= readLen) {
            System.arraycopy(originData, 0, data, 0, readLen);
        } else {
            System.arraycopy(originData, 0, data, readLen - originData.length, originData.length);
        }
        int cmdLen = 16 + data.length;
        int parameterLen = 9 + data.length;
        byte[] cmd = new byte[cmdLen];
        cmd[0] = HEAD;
        cmd[1] = 0x00;
        cmd[2] = 0x49;
        if (parameterLen < 256) {
            cmd[3] = 0x00;
            cmd[4] = (byte) parameterLen;
        } else {
            int paraH = parameterLen / 256;
            int paraL = parameterLen % 256;
            cmd[3] = (byte) paraH;
            cmd[4] = (byte) paraL;
        }
        System.arraycopy(password, 0, cmd, 5, 4);
        cmd[9] = (byte) menBack;
        if (startAddr < 256) {
            cmd[10] = 0x00;
            cmd[11] = (byte) startAddr;
        } else {
            int startH = startAddr / 256;
            int startL = startAddr % 256;
            cmd[10] = (byte) startH;
            cmd[11] = (byte) startL;
        }
        if (dataLen < 256) {
            cmd[12] = 0x00;
            cmd[13] = (byte) dataLen;
        } else {
            int dataLenH = dataLen / 256;
            int dataLenL = dataLen % 256;
            cmd[12] = (byte) dataLenH;
            cmd[13] = (byte) dataLenL;
        }
        System.arraycopy(data, 0, cmd, 14, data.length);
        cmd[cmdLen - 2] = Tools.checkSum(cmd);
        cmd[cmdLen - 1] = END;
        return cmd;
    }


    public static CMDFactory create() {
        return new CMDFactory();
    }

}
