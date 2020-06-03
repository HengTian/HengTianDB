package online.hengtian.memory;

import online.hengtian.myperl.ExcuteResult;
import online.hengtian.mytree.BPlusTree;
import online.hengtian.mytree.Node;
import online.hengtian.mytree.RowIndex;
import online.hengtian.table.TableBean;
import online.hengtian.utils.ByteArrayUtils;
import online.hengtian.utils.JavaBeanUtils;
import online.hengtian.utils.MyFileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static online.hengtian.memory.DbSystem.*;
import static online.hengtian.memory.DbSystem.PAGE_SIZE;
import static online.hengtian.memory.DbSystem.TABLE_LINE_NUM;

/**
 * 操作表
 */
public class Tabler<T extends TableBean> {
    /**
     * 读取.table文件，返回一个table的实例
     * @param fileName
     * @return
     */

    public Table dbOpen(String fileName,Boolean isCreate) throws IOException {
        Table t=tableRead(fileName,isCreate);
        DB_TABLES.put(fileName,t);
        return t;
    }
    public ExcuteResult drop(String tableName) {
        if(DB_TABLES.get(tableName)!=null) {
            DB_TABLES.remove(tableName);
        }else{
            return ExcuteResult.EXCUTE_TABLE_NOT_EXIST;
        }
        String fileTable=tableName+DB_TABLE_SUFFIX;
        String fileDB=tableName+DB_STORAGE_SUFFIX;
        String fileClass=tableName+DB_STORAGE_SUFFIX;
        if(MyFileUtils.isFileExists(fileTable)){
            File table=new File(fileTable);
            table.delete();
        }
        if(MyFileUtils.isFileExists(fileDB)){
            File db=new File(fileDB);
            db.delete();
        }
        JavaBeanUtils.deleteTableBean(tableName);
        return ExcuteResult.EXCUTE_SUCCESS;

    }
    public boolean checkTableExists(String tableName,Boolean isCreate){
        /**
         * 如果插入时表不存在，则将表加载到内存中
         */
        if(DB_TABLES.get(tableName)==null){
            Table table= null;
            try {
                table = dbOpen(tableName,isCreate);
            } catch (IOException e) {
                return false;
            }
            if(table==null) return false;
            DB_TABLES.put(tableName,table);
        }
        return true;
    }

    public ExcuteResult insertOrUpdate(String tableName, T bean){
        if(!checkTableExists(tableName,false)) return ExcuteResult.EXCUTE_TABLE_NOT_EXIST;
        Table table = DB_TABLES.get(tableName);
        //更新ID
        if(bean.getId()==null){
            table.setRowCursor(table.getRowCursor()+1);
            bean.setId(table.getRowCursor());
            table.setNumRows(table.getNumRows()+1);
        }
        System.out.println(bean.toString());
        if(table.getTree()==null){
            table.setTree(new BPlusTree(TREE_LEVEL,TREE_LENGTH));
        }
        table.getTree().insertOrUpdate(bean.getId(),bean);
        Optional<byte[]> s = ByteArrayUtils.objectToBytes(bean);
        table.getIndexTree().insertOrUpdate(bean.getId(),s.get().length);
        return ExcuteResult.EXCUTE_SUCCESS;
    }
    public boolean updatePage(String tableName){
        if(!checkTableExists(tableName,false)) return false;
        Table table = DB_TABLES.get(tableName);
        List<Object> values = table.getTree().getValues();
        List<T> users=new ArrayList<>();
        for (Object user:values) {
            users.add((T)user);
        }
        table.getPager().updatePageRow(users,0,table);
        return true;
    }

    public boolean insertPage(String tableName){
        if(!checkTableExists(tableName,false)) return false;
        Table table = DB_TABLES.get(tableName);
        List<Object> values = table.getTree().getValues();
        for (Object user:values) {
            try {
                insertPageRow((T)user,tableName);
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
    public boolean insertPageRow (T user,String tableName) throws IOException {
        if(!checkTableExists(tableName,false)) return false;
        Table table = DB_TABLES.get(tableName);
        Optional<byte[]>s = ByteArrayUtils.objectToBytes(user);
        if (table.getPager().getPages()==null){
            table.getPager().setPages(new ArrayList<>());
            table.getPager().getPages().add(new Page());
            table.setNumPages(table.getNumPages()+1);
            //tree=new BPlusTree(TREE_LEVEL,TREE_LENGTH);
        }
        //插入到树中
        //tree.insertOrUpdate(user.getId(),user);
        if (s.get().length>PAGE_SIZE){
            return false;
        }
        //如果插入的数据大于一页就暂时处理不了，这个循环最多进行两次
        while (!table.getPage(table.getPager().getPages().size()-1).append(s.get())&&s.get().length <= PAGE_SIZE) {
            table.getPager().getPages().add(new Page());
            table.setNumPages(table.getNumPages()+1);
        }
        //标记该页是否被修改
//        table.getPage(table.getPager().getPages().size()-1).setModify(true);
        table.setModify(true);
        //插入索引树
        table.getIndexTree().insertOrUpdate(user.getId(),s.get().length);
        table.setNumRows(table.getNumRows()+1);
        return true;
    }
    public ExcuteResult delete(String tableName,List<String> params){
        if(!checkTableExists(tableName,false)) return ExcuteResult.EXCUTE_TABLE_NOT_EXIST;
        Table table = DB_TABLES.get(tableName);
        int begin,end;
        if(table.getTree()==null){
            return ExcuteResult.EXCUTE_TABLE_NULL;
        }
        if(params==null||params.size()<1){
            return ExcuteResult.EXCUTE_PARAM_ERROR;
        }else if(params.size()==1){
            begin=Integer.parseInt(params.get(0));
            end=begin;
        }else if(params.size()==2){
            begin=Integer.parseInt(params.get(0));
            end=Integer.parseInt(params.get(1));
        }else{
            return ExcuteResult.EXCUTE_PARAM_ERROR;
        }
        for(long i=begin;i<=end;i++){
            table.getTree().remove(i);
            RowIndex rowIndex = table.getIndexTree().get(i);
            Map.Entry<Comparable, Object> entry = rowIndex.getNode().getData().get(rowIndex.getIndex());
            table.getKeys().remove((Object)entry.getKey());
            table.getIndexs().remove((Object)entry.getValue());
            table.getIndexTree().remove(i);
            table.setNumRows(table.getNumRows()-1);
        }
        DB_TABLES.put(tableName,table);
        return ExcuteResult.EXCUTE_SUCCESS;
    }
    public ExcuteResult select(String tableName,List<String> params){
        if(!checkTableExists(tableName,false)) return ExcuteResult.EXCUTE_TABLE_NOT_EXIST;
        Table table = DB_TABLES.get(tableName);
        Comparable begin,end;
        if(params==null||params.size()==0){
            begin=0L;
            end=table.getRowCursor();
        }else if(params.size()==1){
            begin=Long.parseLong(params.get(0));
            end=table.getRowCursor();
        }else if(params.size()==2){
            begin=Long.parseLong(params.get(0));
            end=Long.parseLong(params.get(1));
        }else{
            return ExcuteResult.EXCUTE_PARAM_ERROR;
        }
        if(table.getTree()!=null){
            table.getTree().printTree(begin,end);
        }
        return ExcuteResult.EXCUTE_SUCCESS;
    }
    public List<T> getUsers(Table table) throws IOException, ClassNotFoundException {
        if(table.getNumRows()==0){
            return null;
        }
        Page page;
        int pageIndex=0;
        int rowIndex=0;
        System.out.println("the total rows : "+table.getNumRows());
        List<T> users=new ArrayList<>();
        int rows=table.getNumRows();
        int begin=0;
        System.out.println("current page : "+pageIndex);
        while(rows>0){
            if((begin+(int)(table.getIndexs().get(rowIndex)))>PAGE_SIZE) {
                pageIndex++;
                System.out.println("current page : "+pageIndex);
                begin=0;
            }
            page=table.getPage(pageIndex);
            Byte[] b=new Byte[(int)(table.getIndexs().get(rowIndex))];
            Optional<Object> optionalO = ByteArrayUtils.bytesToObject(ByteArrayUtils.toPrimitives(page.getContent().subList(begin, begin + (int)(table.getIndexs().get(rowIndex))).toArray(b)));
            T user = (T) optionalO.get();
//            System.out.println(user);
            begin+=(int)(table.getIndexs().get(rowIndex));
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
    public boolean dbClose(String tableName) {
        if(!checkTableExists(tableName,false)) return false;
        Table table = DB_TABLES.get(tableName);
        try {
            //更新页中的数据与表结构
            tableWrite(tableName,table);
            //把树中的数据插入到Page里
            updatePage(tableName);
            if(table.getIndexs().size()==0) return true;
            table.getPager().pageWrite(tableName,0,table.getNumPages());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 读table后维护一个全部行的indexTree
     * @param keys
     * @param indexs
     */
    public void indexRead(List<Comparable> keys,List<Integer> indexs,Table table){
        if(table.getIndexTree()==null){
            table.setIndexTree(new BPlusTree(TREE_LEVEL,TREE_LENGTH));
        }
        for(int i=0;i<keys.size();i++){
            table.getIndexTree().insertOrUpdate(keys.get(i),indexs.get(i));
        }
        //indexTree.printTree();
    }

    public void treeRead(Pager p,Table table){
        if(table.getTree()==null){
            table.setTree(new BPlusTree(TREE_LEVEL,TREE_LENGTH));
        }
        List<T> users=null;
        try {
            users = getUsers(table);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if(users!=null){
            users.stream().forEach(user->table.getTree().insertOrUpdate(user.getId(),user));
        }
    }
    public Table tableRead(String fileName,Boolean isCreate) throws IOException {
        String file = fileName + DB_TABLE_SUFFIX;
        Table t = new Table();
        if (MyFileUtils.isFileExists(file)) {
            System.out.println("读取table");
            RandomAccessFile fd = new RandomAccessFile(file, "r");
            //获取文件中表信息的长度
            int len = fd.readInt();
            byte[] content = new byte[len];
            fd.seek(TABLE_LINE_NUM);
            fd.read(content);
            t = (Table) ByteArrayUtils.bytesToObject(content).get();
            //组织记录数据行长度的树
            indexRead(t.getKeys(), t.getIndexs(),t);
            fd.close();
            Pager p = new Pager().pagerOpen(t, fileName);
            t.setPager(p);
            //组织数据树
            treeRead(p,t);
        } else {
            if(isCreate) {
                t.setIndexs(new ArrayList<>());
                t.setKeys(new ArrayList());
                t.setNumRows(0);
                t.setNumPages(0);
                t.setRowCursor(0L);
                t.setPager(new Pager());
                t.setTree(new BPlusTree(TREE_LEVEL, TREE_LENGTH));
                t.setIndexTree(new BPlusTree(TREE_LEVEL, TREE_LENGTH));
            }else {
                return null;
            }
        }
        return t;
    }
    public boolean tableWrite(String fileName,Table t) throws IOException {
        System.out.println("写入table");
        t.setIndexs(new ArrayList<>());
        RandomAccessFile fd = new RandomAccessFile(fileName+DB_TABLE_SUFFIX, "rw");
        t.setKeys(t.getIndexTree().getKeys());
        t.setNumRows(t.getKeys().size());
        //写入indexs
        Node head=t.getIndexTree().getHead();
        while(head!=null) {
            for (Map.Entry entry : head.getData()) {
                t.getIndexs().add(entry.getValue());
            }
            head=head.getNext();
        }
        t.setNumPages(updatePageNum(t.getIndexs()));
        Optional<byte[]> s = ByteArrayUtils.objectToBytes(t);
        System.out.println(t);
        int len = s.get().length;
        System.out.println("正在关闭表"+fileName);
        System.out.println("关闭表的长度: "+len);
        System.out.println("关闭表的结构: "+ (Table) ByteArrayUtils.bytesToObject(s.get()).get());
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


}
