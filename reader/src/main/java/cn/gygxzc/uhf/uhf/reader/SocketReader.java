package cn.gygxzc.uhf.uhf.reader;

import android.util.Log;

import java.io.InputStream;
import java.util.Arrays;

import cn.gygxzc.uhf.uhf.util.Tools;

/**
 * 读取数据
 */
public class SocketReader {


    private static final byte HEAD = (byte) 0xAA;
    private static final byte END = (byte) 0x8E;

    /**
     * 500ms内轮询无数据则，无数据返回
     *
     * @param inputStream 一个指令一个回复，如果没有回复，则报错
     * @return 返回的消息
     */
    public static byte[] read(InputStream inputStream) {
        int size = 0;
        byte[] buffer = new byte[256];
        byte[] temp = new byte[512];
        byte[] resp = null;
        int index = 0;  //temp有效数据指向
        int count = 0;  //temp有效数据长度
        int cnt = 6;
        try {
            while (cnt > 0) {
                Thread.sleep(150);
                int ts = inputStream.available();
                if (ts <= 0) {
                    cnt--;
                    continue;
                }
                size = inputStream.read(buffer, 0, 256);
                if (size > 0) {
                    Log.e("buffer--", Tools.Bytes2HexString(buffer, size));
                    count += size;
                    //超出temp长度清空
                    if (count > 512) {
                        count = 0;
                        Arrays.fill(temp, (byte) 0x00);
                    }
                    //先将接收到的数据拷到temp中
                    System.arraycopy(buffer, 0, temp, index, size);
                    index = index + size;
                    if (count > 7) {
                        if (temp[0] == HEAD) {
                            int len = temp[4] & 0xff;
                            if (count < len + 7) {//数据区尚未接收完整
                                continue;
                            }
                            if (temp[len + 6] != END) {//数据区尚未接收完整
                                continue;
                            }
                            //得到完整数据包
                            resp = new byte[len + 7];
                            System.arraycopy(temp, 0, resp, 0, len + 7);
                            Arrays.fill(temp, (byte) 0x00);
                            return resp;
                        } else {
                            //包错误清空
                            count = 0;
                            index = 0;
                            Arrays.fill(temp, (byte) 0x00);
                        }
                    }
                }
                cnt--;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return resp;
    }
}
