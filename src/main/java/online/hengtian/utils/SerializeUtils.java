package online.hengtian.utils;

import online.hengtian.table.User;

import java.io.*;
import java.util.Optional;

/**
 * @author 陆子恒
 * @version 1.0
 * @date 2020/4/15 10:33
 * @description 先手动序列化吧，蓝瘦
 */
public class SerializeUtils {

    public static String serialize(Object obj) throws IOException {
        Optional<byte[]> bytes = ByteArrayUtils.objectToBytes(obj);//将对象转换为二进制字节数组
        byte[] ret = bytes.get();
        String string = ByteArrayUtils.toHexString(ret);
        return string;
    }
    public static Object serializeToObject(String str) throws IOException, ClassNotFoundException {
        byte[] r2 = ByteArrayUtils.toByteArray(str);
        Optional<Object> object = ByteArrayUtils.bytesToObject(r2);
        return object.get();
    }
}
