package online.hengtian.utils;

import online.hengtian.memory.Table;

import java.io.*;

import static online.hengtian.memory.DbSystem.DB_REDO_LOG;
import static online.hengtian.memory.DbSystem.TABLE_LINE_NUM;

/**
 * @author <a href="lzh@sq108.com">陆子恒</a>
 * @version 1.0
 * @date 2020/4/16 13:06
 * @description
 */
public class MyFileUtils {
    public static boolean isFileExists(String fileName){
        File file=new File(fileName);
        if(file.exists()){
            return true;
        } else {
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
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

    public static void writeLine(String s){
        FileWriter fw = null;
        try {
        //如果文件存在，则追加内容；如果文件不存在，则创建文件
            File f=new File(DB_REDO_LOG);
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        pw.println(s);
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
