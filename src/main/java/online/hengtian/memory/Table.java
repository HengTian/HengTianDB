package online.hengtian.memory;

import lombok.Data;
import online.hengtian.mytree.BPlusTree;
import online.hengtian.table.TableBean;
import java.io.Serializable;
import java.util.*;


/**
 * @author 陆子恒
 * @version 1.0
 * @date 2020/4/15 10:06
 */
@Data
public class Table<T extends TableBean> implements Serializable {

    private static final long serialVersionUID = 5256253515960413663L;
    /**
     * 这个树存具体的对象
     */
    private transient BPlusTree tree;
    /**
     * 这个树中存主键与长度
     */
    private transient BPlusTree indexTree;
    /**
     * 表中的行数
     */
    private Integer numRows;
    /**
     * 记录索引的指针
     */
    private Long rowCursor;
    /**
     * 表中的页数
     */
    private Integer numPages;
    /**
     * 存储每一行数据的键
     */
    private List<Comparable> keys;
    /**
     * 存储每一行数据的长度
     */
    private List<Integer> indexs;
    /**
     * pager负责在打开文件时将数据加载到内存并放到table中
     * 下面和磁盘打交道的希望都放到pager中，暂时还没完成
     */
    private transient Pager pager;
    /**
     * 在已有数据的基础上标识是否进行了修改操作如insert
     * 未修改则不进行操作
     */
    private transient boolean isModify=false;

    public Page getPage(int index){
        return getPager().getPage(index);
    }
    @Override
    public String toString() {
        return "Table{" +
                "numRows=" + numRows +
                ", numPages=" + numPages +
                ", rowCursor=" + rowCursor +
                ", indexs=" + Arrays.toString(indexs.toArray()) +
                ", keys=" + Arrays.toString(keys.toArray()) +
                ", pager=" + pager +
                '}';
    }

}
