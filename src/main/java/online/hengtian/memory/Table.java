package online.hengtian.memory;

import online.hengtian.table.User;
import online.hengtian.utils.ByteArrayUtils;
import online.hengtian.utils.MyFileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static online.hengtian.memory.DbSystem.*;

/**
 * @author 陆子恒
 * @version 1.0
 * @date 2020/4/15 10:06
 */
public class Table<T extends Comparable> implements Serializable {

    private static final long serialVersionUID = 5256253515960413663L;
    /**
     * 表中的行数
     */
    private Integer numRows;
    /**
     * 表中的页数
     */
    private Integer numPages;
    /**
     * 存储每一行数据的键
     */
    private List<T> keys;
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
    /**
     * 读取.table文件，返回一个table的实例
     * @param fileName
     * @return
     */
    public Table dbOpen(String fileName){
        Table t=new Table();
        try {
            t=tableRead(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return t;
    }

    /**
     * 插入用户
     * @param user
     * @throws IOException
     */
    public boolean insert (User user) throws IOException {
        Optional<byte[]> s = ByteArrayUtils.objectToBytes(user);
        if (getPager().getPages()==null){
            getPager().setPages(new ArrayList<>());
            getPager().getPages().add(new Page());
            numPages++;
        }
        if (s.get().length>PAGE_SIZE){
            return false;
        }
        //如果插入的数据大于一页就暂时处理不了，这个循环最多进行两次
        while (!getPage(getPager().getPages().size()-1).append(s.get())&&s.get().length <= PAGE_SIZE) {
            getPager().getPages().add(new Page());
            numPages++;
        }
        //标记该页是否被修改
        getPage(getPager().getPages().size()-1).setModify(true);
        setModify(true);
        indexs.add(s.get().length);
        numRows++;
        return true;
    }
    public List<User> select() throws IOException, ClassNotFoundException {
        if(numRows==0){
            return null;
        }
        Page page;
        int pageIndex=0;
        int rowIndex=0;
        System.out.println("the total rows : "+numRows);
        List<User> users=new ArrayList<>();
        int rows=numRows;
        int begin=0;
        while(rows>0){
            if((begin+indexs.get(rowIndex))>PAGE_SIZE) {
                pageIndex++;
                begin=0;
            }
            page=getPage(pageIndex);
            System.out.println("current page : "+pageIndex);
            Byte[] b=new Byte[indexs.get(rowIndex)];
            Optional<Object> optionalO = ByteArrayUtils.bytesToObject(ByteArrayUtils.toPrimitives(page.getContent().subList(begin, begin + indexs.get(rowIndex)).toArray(b)));
            User user = (User) optionalO.get();
            System.out.println(user);
            begin+=indexs.get(rowIndex);
            users.add(user);
            rows--;
            rowIndex++;
        }
        return users;
    }

    /**
     * 关闭数据库时将table实例写入.table文件,将表的数据写入页中,将简化的B+树结构存储到文件中
     * @
     * @return
     * @throws IOException
     */
    public boolean dbClose(String fileName) {
        if(isModify()) {
            try {
                tableWrite(fileName);
                getPager().pageWrite(fileName,0,getNumPages());
                getPager().bTreeWrite(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return true;
    }


    public Table tableRead(String fileName) throws IOException {
        String file=fileName+DB_TABLE_SUFFIX;
        Table t=new Table();
        if(MyFileUtils.isFileExists(file)) {
            System.out.println("读取table");
            RandomAccessFile fd = new RandomAccessFile(file, "r");
            int len = fd.readInt();
            byte[] content = new byte[len];
            fd.seek(TABLE_LINE_NUM);
            fd.read(content);
            t = (Table) ByteArrayUtils.bytesToObject(content).get();
            System.out.println(t);
            fd.close();
            Pager p=new Pager().pagerOpen(t,fileName);
            t.setPager(p);
        }else{
            t.setIndexs(new ArrayList<>());
            t.setKeys(new ArrayList());
            t.setNumRows(0);
            t.setNumPages(0);
            t.setPager(new Pager());
        }
        return t;
    }
    public boolean tableWrite(String fileName) throws IOException {
        System.out.println("写入table");
        RandomAccessFile fd = new RandomAccessFile(fileName+DB_TABLE_SUFFIX, "rw");
        Optional<byte[]> s = ByteArrayUtils.objectToBytes(this);
        int len = s.get().length;
        System.out.println("关闭数据库时表的长度: "+len);
        System.out.println("关闭数据库时表的结构: "+ (Table) ByteArrayUtils.bytesToObject(s.get()).get());
        fd.seek(0);
        fd.writeInt(len);
        fd.seek(TABLE_LINE_NUM);
        fd.write(s.get());
        fd.close();
        return true;
    }

    public Page getPage(int index){
        return getPager().getPage(index);
    }
    @Override
    public String toString() {
        return "Table{" +
                "numRows=" + numRows +
                ", numPages=" + numPages +
                ", indexs=" + Arrays.toString(indexs.toArray()) +
                ", keys=" + Arrays.toString(keys.toArray()) +
                ", pager=" + pager +
                '}';
    }
    public List<Integer> getIndexs() {
        return indexs;
    }

    public void setIndexs(List<Integer> indexs) {
        this.indexs = indexs;
    }

    public boolean isModify() {
        return isModify;
    }

    public void setModify(boolean modify) {
        isModify = modify;
    }

    public Integer getNumPages() {
        return numPages;
    }

    public void setNumPages(Integer numPages) {
        this.numPages = numPages;
    }

    public Pager getPager() {
        return pager;
    }

    public void setPager(Pager pager) {
        this.pager = pager;
    }

    public Integer getNumRows() {
        return numRows;
    }

    public void setNumRows(Integer numRows) {
        this.numRows = numRows;
    }

    public List<T> getKeys() {
        return keys;
    }

    public void setKeys(List<T> keys) {
        this.keys = keys;
    }
}
