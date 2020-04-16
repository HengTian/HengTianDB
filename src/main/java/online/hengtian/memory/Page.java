package online.hengtian.memory;

import online.hengtian.myperl.FrontEnd;
import online.hengtian.utils.ByteArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static online.hengtian.memory.DbSystem.PAGE_SIZE;

/**
 * @author 陆子恒
 * @version 1.0
 * @date 2020/4/15 10:05
 */
public class Page {
    private volatile transient List<Byte> content;
    private boolean isModify=false;
    public Boolean append(byte[] s){
        if (content==null){
            content=new ArrayList<>();
        }
        if (content.size()+s.length < PAGE_SIZE) {
            for(byte b:s){
                content.add(b);
            }
            System.out.println("the page used : "+content.size());
            return true;
        }
        return false;
    }

    public byte[] getBytes(){
        Byte[] b=new Byte[getContent().size()];
        getContent().toArray(b);
        return ByteArrayUtils.toPrimitives(b);
    }
    public List<Byte> getContent() {
        return content;
    }

    public void setContent(List<Byte> content) {
        this.content = content;
    }

    public boolean isModify() {
        return isModify;
    }

    public void setModify(boolean modify) {
        isModify = modify;
    }
}
