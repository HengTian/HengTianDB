package online.hengtian.utils;

import online.hengtian.memory.Table;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static online.hengtian.memory.DbSystem.TABLE_LINE_NUM;

/**
 * @author <a href="lzh@sq108.com">陆子恒</a>
 * @version 1.0
 * @date 2020/4/16 13:06
 * @description
 */
public class FileUtils {
    public static boolean isFileExists(String fileName){
        File file=new File(fileName);
        if(file.exists()){
            return true;
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
    public static byte[] getContent(RandomAccessFile fd,int offset,int length){
        //读出文件中表的信息
        try {
            byte[] content=new byte[length];
            fd.seek(offset);
            fd.read(content);
            return content;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
