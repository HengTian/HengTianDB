package online.hengtian.memory;

import lombok.Data;
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
@Data
public class Page {
    private volatile transient List<Byte> content;
    private String tableName;
    private Integer index;
    private boolean isWrite=false;
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
}
