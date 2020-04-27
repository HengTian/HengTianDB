package online.hengtian.memory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="lzh@sq108.com">陆子恒</a>
 * @date 2020/4/26 10:27
 */
public class LRUList{
    LinkedHashMap<Comparable,Page> oldList;
    LinkedHashMap<Comparable,Page> youngList;
    private int size;

    /**
     *
     * @param size LRUList的总大小 old+young
     * @param rate old与young的size比例，如果是0.2,则old:young=2:8
     */
    public LRUList(int size,float rate) {
        this.size=size;
        int oldLen= (int) (size*rate);
        oldList=new LinkedHashMap<>(oldLen);
        youngList=new LinkedHashMap<>(size-oldLen);
    }

    public Page get(Comparable key){
        Page result;
        if(youngList.containsKey(key)){
            result=youngList.get(key);
            youngList.remove(key);
            youngList.put(key,result);
            return result;
        }else if(oldList.containsKey(key)){
            result=oldList.get(key);
            oldList.remove(key);
            youngList.put(key,result);
        }else{
            return null;
        }
        return null;
    }

    public void put(Comparable key,Page page){
        if(youngList.containsKey(key)){
            youngList.remove(key);
            youngList.put(key,page);
        }else if(oldList.containsKey(key)){
            oldList.remove(key);
            youngList.put(key,page);
        }else{
            oldList.put(key,page);
        }
    }

    public Page getPageKey(Comparable key){
        Set<Comparable> keys=new TreeSet<>();
        keys.addAll(youngList.keySet());
        keys.addAll(oldList.keySet());
        Iterator<Comparable> it = keys.iterator();
        while (it.hasNext()) {
            Comparable next = it.next();
            if(key.compareTo(next)>0){
                if(!it.hasNext()) {
                    return get(next);
                }
                if(key.compareTo(it.next())<=0){
                    return get(next);
                }
            }
        }
        return null;
    }
}
