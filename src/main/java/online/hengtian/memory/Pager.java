package online.hengtian.memory;

import online.hengtian.table.TableBean;
import online.hengtian.table.User;
import online.hengtian.utils.ByteArrayUtils;
import online.hengtian.utils.MyFileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static online.hengtian.memory.DbSystem.*;

/**
 * @author <a href="lzh@sq108.com">陆子恒</a>
 * @version 1.0
 * @date 2020/4/16 9:19
 * @description 表文件的第一页存储表的基本信息：行数,
 */
public class Pager {
    private List<Page> pages;

    public Pager() {
        pages=new ArrayList<>();
    }

    public Pager pagerOpen(Table t, String fileName){
        //将磁盘中的数据刷到内存中，后面可以加上限制
        pages=new ArrayList<>();
        List<Page> pagesList = getPages(t.getIndexs(), fileName, 0, t.getNumPages());
        pages.addAll(pagesList);
        //System.out.println(getPageNum(21,t.getIndexs()));
        return this;
    }

    /**
     * 获取某几页的数据
     * @param from
     * @param to
     * @return
     */
    public List<Page> getPages(List<Integer> indexs,String fileName,int from,int to){
        List<Page> pages=new ArrayList<>();
        //将磁盘中的数据刷到内存中，后面可以加上限制
        try (RandomAccessFile fd=new RandomAccessFile(fileName+DB_STORAGE_SUFFIX,"r")){
            for(int pageIndex=from;pageIndex<to;pageIndex++) {
                byte[] content = MyFileUtils.getContent(fd, PAGE_SIZE * pageIndex, PAGE_SIZE);
                int sum=0;
                if(pageIndex==to-1){
                    for(int i=0;i<indexs.size();i++){
                        if(sum+indexs.get(i)>PAGE_SIZE){
                            sum=0;
                        }
                        sum+=indexs.get(i);
                    }
                    content= MyFileUtils.getContent(fd, PAGE_SIZE * pageIndex, sum);
                }
                System.out.println("page: "+pageIndex+" length:"+content.length);
                Page page=new Page();
                page.setContent(ByteArrayUtils.toByteList(content));
                pages.add(page);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pages;
    }
    /**
     * 关闭数据库时将table实例写入.table文件
     * @param fileName
     * @return
     */
    public boolean pageWrite(String fileName,int from,int numPage) throws IOException {
        System.out.println("写入page");
        RandomAccessFile fd = new RandomAccessFile(fileName+DB_STORAGE_SUFFIX, "rw");
        for (int i = from; i < numPage; i++) {
            fd.seek(PAGE_SIZE * i);
            fd.write(getPage(i).getBytes());
        }
        fd.close();
        return true;
    }
    public void addPage(Page page){
        if(getPages()==null){
            setPages(new ArrayList<>());
        }
        getPages().add(page);
    }
    public <T extends TableBean> boolean updatePageRow(List<T> user, Integer index, Table t){
        if(getPages().size()==0){
            getPages().add(new Page());
        }
        this.getPages().set(index,new Page());
        for(int i=0;i<user.size();i++){
            Optional<byte[]>s = ByteArrayUtils.objectToBytes(user.get(i));
            if (s.get().length>PAGE_SIZE){
                return false;
            }
            //如果插入的数据大于一页就暂时处理不了，这个循环最多进行两次
            while (!getPage(index).append(s.get())&&s.get().length <= PAGE_SIZE) {
                index++;
                if(getPages().size()>index){
                    getPages().set(index,new Page());
                }else{
                    getPages().add(new Page());
                }

            }
        }
        return true;
    }
    public int getPageNum(int rowNum,List<Integer> indexs){
        int sum=0,pageNum=0;
        for(int i=0;i<=rowNum;i++){
            if(sum+indexs.get(i)>PAGE_SIZE){
                pageNum++;
                sum=0;
            }
            sum+=indexs.get(i);
        }
        return pageNum;
    }
    public Page getPage(int index){
        return getPages().get(index);
    }
    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }
}
