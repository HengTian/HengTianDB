package online.hengtian.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 陆子恒
 * @version 1.0
 * @date 2020/4/15 10:48
 */
public class DbSystem {
    public final static Integer PAGE_SIZE = 1024*4;
    public final static String TABLE_INSERT="insert";
    public final static String TABLE_SELECT="select";
    public final static String TABLE_DELETE="delete";
    public final static String TABLE_UPDATE="update";
    public final static String TABLE_CREATE="create";
    public final static String TABLE_DROP="drop";
    public final static String DB_STORAGE_SUFFIX=".db";
    public final static String DB_TABLE_SUFFIX=".table";
    public final static String DB_REDO_LOG="redo.log";
    public final static Integer TABLE_LINE_NUM=5;
    public final static Integer TREE_LEVEL=4;
    public final static Integer TREE_LENGTH=4;
    public final static Map<String,Table> DB_TABLES=new ConcurrentHashMap<>();
    public final static String TABLE_STORAGE_PACKAGE="online.hengtian.tablebean";
    public final static String ROOT_PATH="D:/Users/HengTian/Desktop/HengTianDB/src/main/java/";
    public final static String TARGET_DIR="D:/Users/HengTian/Desktop/HengTianDB/target/classes";
}
