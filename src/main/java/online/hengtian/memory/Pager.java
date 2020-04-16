package online.hengtian.memory;

import online.hengtian.table.User;
import online.hengtian.utils.ByteArrayUtils;
import online.hengtian.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static online.hengtian.memory.DbSystem.PAGE_SIZE;
import static online.hengtian.memory.DbSystem.TABLE_LINE_NUM;

/**
 * @author <a href="lzh@sq108.com">陆子恒</a>
 * @version 1.0
 * @date 2020/4/16 9:19
 * @description 表文件的第一页存储表的基本信息：行数,
 */
public class Pager {
    private List<Page> pages;
    public Pager pagerOpen(Table t,String fileName,int len){
        Pager p=new Pager();
        //将磁盘中的数据刷到内存中，后面可以加上限制
        try (RandomAccessFile fd=new RandomAccessFile(fileName,"r")){
            for(int pageIndex=1;pageIndex<=t.getNumPages();pageIndex++) {
                byte[] content = FileUtils.getContent(fd, TABLE_LINE_NUM + PAGE_SIZE * (len/PAGE_SIZE+pageIndex), PAGE_SIZE);
                int sum=0;
                if(pageIndex==t.getNumPages()){
                    for(int i=0;i<t.getNumRows();i++){
                        if(sum+t.getIndexs().get(i)>PAGE_SIZE){
                            sum=0;
                        }
                        sum+=t.getIndexs().get(i);
                    }
                    content= FileUtils.getContent(fd, TABLE_LINE_NUM + PAGE_SIZE * (len/PAGE_SIZE+pageIndex), sum);
                }
                System.out.println("page "+pageIndex+" length:"+content.length);
                Page page=new Page();
                page.setContent(ByteArrayUtils.toByteList(content));
                t.addPage(page);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return p;
    }

    public void addPage(Page page){
        if(getPages()==null){
            setPages(new ArrayList<>());
        }
        getPages().add(page);
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }
}
