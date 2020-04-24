package online.hengtian.memory;

/**
 * @author 陆子恒
 * @version 1.0
 * @date 2020/4/15 10:48
 */
public class DbSystem {
    public final static Integer PAGE_SIZE = 1024*4;
    public final static String TABLE_INSERT="insert";
    public final static String TABLE_SELECT="select";
    public final static String DB_STORAGE_SUFFIX=".db";
    public final static String DB_TABLE_SUFFIX=".table";
    public final static String DB_BTREE_SUFFIX=".b";
    public final static String DB_REDO_LOG="redo.log";
    public final static Integer TABLE_LINE_NUM=4;

}
