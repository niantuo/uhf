package cn.gygxzc.uhf.uhf.util;


public class Tools {

    //byte 转十六进制
    public static String Bytes2HexString(byte[] b, int size) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            ret.append(hex.toUpperCase());
        }
        return ret.toString();
    }

    public static String bytes2HexString(byte[] bytes) {
        return Bytes2HexString(bytes, bytes.length);
    }

    private static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0}));
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1}));
        return (byte) (_b0 ^ _b1);
    }

    //十六进制转byte
    public static byte[] HexString2Bytes(String src) {
        int len = src.length() / 2;
        byte[] ret = new byte[len];
        byte[] tmp = src.getBytes();

        for (int i = 0; i < len; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    /* byte[]转Int */
    public static int bytesToInt(byte[] bytes) {
        int addr = bytes[0] & 0xFF;
        addr |= ((bytes[1] << 8) & 0xFF00);
        addr |= ((bytes[2] << 16) & 0xFF0000);
        addr |= ((bytes[3] << 25) & 0xFF000000);
        return addr;

    }

    /* Int转byte[] */
    public static byte[] intToByte(int i) {
        byte[] abyte0 = new byte[3];
        abyte0[2] = (byte) (0xff & i);
        abyte0[1] = (byte) ((0xff00 & i) >> 8);
        abyte0[0] = (byte) ((0xff0000 & i) >> 16);
        return abyte0;
    }

    //计算校验和
    public static byte checkSum(byte[] data) {
        byte crc = 0x00;
        // 从指令类型累加到参数最后一位
        for (int i = 1; i < data.length - 2; i++) {
            crc += data[i];
        }
        return crc;
    }

}
