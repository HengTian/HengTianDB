package online.hengtian.memory;

import online.hengtian.table.User;
import online.hengtian.utils.ByteArrayUtils;
import online.hengtian.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static online.hengtian.memory.DbSystem.PAGE_SIZE;
import static online.hengtian.memory.DbSystem.TABLE_LINE_NUM;

/**
 * @author 陆子恒
 * @version 1.0
 * @date 2020/4/15 10:06
 */
public class Table implements Serializable {
    private static final long serialVersionUID = 5256253515960413663L;
    private Integer numRows;
    private Integer numPages;
    private transient List<Page> pages;
    private List<Integer> indexs;
    private transient Pager pager;
    private transient boolean isModify=false;
    /**
     * @description 返回一个table的实例
     * @param fileName
     * @return
     */
    public Table dbOpen(String fileName){
        Table t=new Table();
        if(FileUtils.isFileExists(fileName)){
            RandomAccessFile fd= null;
            //读出文件中表的信息
            try {
                fd = new RandomAccessFile(fileName,"r");
                int len=fd.readInt();
                byte[] content=new byte[len];
                fd.seek(TABLE_LINE_NUM);
                fd.read(content);
                t=(Table)ByteArrayUtils.bytesToObject(content).get();
                System.out.println(t);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Pager p=new Pager().pagerOpen(t,fileName);
            t.setPager(p);
        } else {
            t.setIndexs(new ArrayList<>());
            t.setNumRows(0);
            t.setNumPages(0);
        }
        return t;
    }
    public void insert (User user) throws IOException {
        Optional<byte[]> s = ByteArrayUtils.objectToBytes(user);
        if(pages==null){
            pages=new ArrayList<>();
            pages.add(new Page());
            numPages++;
        }
        //如果插入的数据大于一页就暂时处理不了，这个循环最多进行两次
        while (!pages.get(pages.size()-1).append(s.get())&&s.get().length <= PAGE_SIZE) {
            pages.add(new Page());
            numPages++;
        }
        //标记该页是否被修改
        pages.get(pages.size()-1).setModify(true);
        setModify(true);
        indexs.add(s.get().length);
        numRows++;
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
            if((begin+indexs.get(rowIndex))>=(pageIndex+1)*PAGE_SIZE) {
                pageIndex++;
                begin=0;
            }
            page=pages.get(pageIndex);
            System.out.println("page : "+pageIndex);
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
    public boolean dbClose(String fileName) throws IOException {
        if(isModify()) {
            RandomAccessFile fd = new RandomAccessFile(fileName, "rw");
            Optional<byte[]> s = ByteArrayUtils.objectToBytes(this);
            int len = s.get().length;
            System.out.println(len);
            System.out.println((Table) ByteArrayUtils.bytesToObject(s.get()).get());
            fd.seek(0);
            fd.writeInt(len);
            fd.seek(TABLE_LINE_NUM);
            fd.write(s.get());
            for (int i = 1; i <= getNumPages(); i++) {
                if (getPages().get(i - 1).isModify()) {
                    fd.seek(TABLE_LINE_NUM + PAGE_SIZE * i);
                    fd.write(getPages().get(i - 1).getBytes());
                }
            }
        }
        return true;
    }
    public void addPage(Page page){
        if(getPages()==null){
            setPages(new ArrayList<>());
        }
        getPages().add(page);
    }
    @Override
    public String toString() {
        return "Table{" +
                "numRows=" + numRows +
                ", numPages=" + numPages +
                ", pages=" + pages +
                ", indexs=" + Arrays.toString(indexs.toArray()) +
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

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }


}
