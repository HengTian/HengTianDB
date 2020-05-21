package online.hengtian.memory;

import lombok.Data;
import online.hengtian.mytree.BPlusTree;
import online.hengtian.mytree.Node;
import online.hengtian.table.TableBean;
import online.hengtian.table.User;
import online.hengtian.utils.ByteArrayUtils;
import online.hengtian.utils.MyFileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.*;

import static online.hengtian.memory.DbSystem.*;

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
    public boolean delete(T bean){
        if(tree.get(bean.getId())==null){
            return false;
        }
        tree.remove(bean.getId());
        return true;
    }
    public boolean insert(T bean){
        if(tree==null){
            tree=new BPlusTree(TREE_LEVEL,TREE_LENGTH);
        }
        if(tree.get(bean.getId())!=null&&!isModify){
            return false;
        }
        tree.insertOrUpdate(bean.getId(),bean);
        Optional<byte[]>s = ByteArrayUtils.objectToBytes(bean);
        indexTree.insertOrUpdate(bean.getId(),s.get().length);
        return true;
    }
    public boolean updatePage(){
        List<Object> values = tree.getValues();
        List<T> users=new ArrayList<>();
        for (Object user:values) {
            users.add((T)user);
        }
        getPager().updatePageRow(users,0,this);
        return true;
    }

    public boolean insertPage(){
        List<Object> values = tree.getValues();
        for (Object user:values) {
            try {
                insertPageRow((T)user);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    /**
     * 页后面追加插入用户，暂时弃用
     * todo 没考虑在中间插入的情况
     * @param user
     * @throws IOException
     */
    public boolean insertPageRow (T user) throws IOException {
        Optional<byte[]>s = ByteArrayUtils.objectToBytes(user);
        if (getPager().getPages()==null){
            getPager().setPages(new ArrayList<>());
            getPager().getPages().add(new Page());
            numPages++;
            //tree=new BPlusTree(TREE_LEVEL,TREE_LENGTH);
        }
        //插入到树中
        //tree.insertOrUpdate(user.getId(),user);
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
        //插入索引树
        indexTree.insertOrUpdate(user.getId(),s.get().length);
        numRows++;
        return true;
    }
    public void select(){
        if(tree!=null){
            tree.printTree();
        }
    }
    public List<User> getUsers() throws IOException, ClassNotFoundException {
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
//        if(isModify()) {
            try {
                //更新页中的数据与表结构
                tableWrite(fileName);
                //把树中的数据插入到Page里
                updatePage();
                getPager().pageWrite(fileName,0,getNumPages());
            } catch (IOException e) {
                e.printStackTrace();
            }

//        }
        return true;
    }

    /**
     * 读table后维护一个全部行的indexTree
     * @param keys
     * @param indexs
     */
    public void indexRead(List<Comparable> keys,List<Integer> indexs){
        if(indexTree==null){
            indexTree=new BPlusTree(TREE_LEVEL,TREE_LENGTH);
        }
        for(int i=0;i<keys.size();i++){
            indexTree.insertOrUpdate(keys.get(i),indexs.get(i));
        }
        //indexTree.printTree();
    }

    public void treeRead(Pager p){
        if(getTree()==null){
            setTree(new BPlusTree(TREE_LEVEL,TREE_LENGTH));
        }
        List<User> users=null;
        try {
           users = getUsers();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if(users!=null){
            users.stream().forEach(user->tree.insertOrUpdate(user.getId(),user));
        }
    }
    public Table tableRead(String fileName) throws IOException {
        String file = fileName + DB_TABLE_SUFFIX;
        Table t = new Table();
        if (MyFileUtils.isFileExists(file)) {
            System.out.println("读取table");
            RandomAccessFile fd = new RandomAccessFile(file, "r");
            int len = fd.readInt();
            byte[] content = new byte[len];
            fd.seek(TABLE_LINE_NUM);
            fd.read(content);
            t = (Table) ByteArrayUtils.bytesToObject(content).get();
            t.indexRead(t.getKeys(), t.getIndexs());
            System.out.println(t);
            fd.close();
            Pager p = new Pager().pagerOpen(t, fileName);
            t.setPager(p);
            t.treeRead(p);
        } else {
            t.setIndexs(new ArrayList<>());
            t.setKeys(new ArrayList());
            t.setNumRows(0);
            t.setNumPages(0);
            t.setPager(new Pager());
            t.setTree(new BPlusTree(TREE_LEVEL, TREE_LENGTH));
            t.setIndexTree(new BPlusTree(TREE_LEVEL,TREE_LENGTH));
        }
        return t;
    }
    public boolean tableWrite(String fileName) throws IOException {
        System.out.println("写入table");
        setIndexs(new ArrayList<>());
        RandomAccessFile fd = new RandomAccessFile(fileName+DB_TABLE_SUFFIX, "rw");
        setKeys(indexTree.getKeys());
        setNumRows(getKeys().size());
        //写入indexs
        Node head=indexTree.getHead();
        while(head!=null) {
            for (Map.Entry entry : head.getEntries()) {
                getIndexs().add((Integer) entry.getValue());
            }
            head=head.getNext();
        }
        setNumPages(updatePageNum(getIndexs()));
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
    public int updatePageNum(List<Integer> indexs) {
        int num=1;
        int sum=0;
        for(int i=0;i<indexs.size();i++){
            if(sum+indexs.get(i)>PAGE_SIZE){
                num++;
                sum=0;
            }
            sum+=indexs.get(i);
        }
        return num;
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
}
