package online.hengtian.memory;

import lombok.Data;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
@Data
public class MyLruCacheWithExpireTime<K, V> {
    private static Boolean isExit=false;

    /**
     * 缓存的最大容量
     */
    private final int maxCapacity;
    private ConcurrentHashMap<K, V> cacheMap;
    private ConcurrentLinkedQueue<K> oldKeys;
    private ConcurrentLinkedQueue<K> youngKeys;
    //flushList满时刷新
    private ConcurrentLinkedQueue<Map.Entry<K,V>> flushList;
    /**
     * 读写锁
     */
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Lock writeLock = readWriteLock.writeLock();
    private Lock readLock = readWriteLock.readLock();
    private ReadWriteLock youngReadWriteLock = new ReentrantReadWriteLock();
    private Lock youngWriteLock = youngReadWriteLock.writeLock();
    private Lock youngReadLock = youngReadWriteLock.readLock();
    private ScheduledExecutorService scheduledExecutorService;
    private Integer oldSize;
    private Integer youngSize;
    private Integer flushSize;

    public MyLruCacheWithExpireTime(int maxCapacity,float rate) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException("Illegal max capacity: " + maxCapacity);
        }
        this.maxCapacity = maxCapacity;
        cacheMap = new ConcurrentHashMap<>(maxCapacity*2);
        oldKeys = new ConcurrentLinkedQueue<>();
        youngKeys = new ConcurrentLinkedQueue<>();
        flushList = new ConcurrentLinkedQueue<>();
        this.oldSize=Math.round(maxCapacity*rate);
        this.youngSize=maxCapacity-this.oldSize;
        this.flushSize=youngSize;
        scheduledExecutorService = Executors.newScheduledThreadPool(oldSize);
        new Thread(()->{
           while(!isExit){
               if(flushList.size()>flushSize){
                   Map.Entry<K, V> poll = flushList.poll();
                   System.out.println("开始写回磁盘第:"+poll.getKey()+"页");
//                   Map.Entry<K, V> poll = flushList.poll();
//                   Page page = poll.getValue();
//                   try {
//                       String tableName = page.getTableName();
//                       Table table = DbSystem.DB_TABLES.get(tableName);
//                       table.getPager().pageWrite(tableName,page.getIndex(),1);
//                       page.setWrite(true);
//                   } catch (IOException e) {
//                       System.out.println("在flushList中写入文件出错");;
//                   }
               }
           }
        }).start();
    }

    public V put(K key, V value, long expireTime) {
        // 加写锁
        writeLock.lock();
        try {
            System.out.println("插入:"+key+":"+value);
            //1.key是否存在于当前缓存
            if (cacheMap.containsKey(key)) {
                if(oldKeys.contains(key)) {
                    moveToTailOfQueue(key,oldKeys);
                }else if(youngKeys.contains(key)){
                    moveToTailOfQueue(key,youngKeys);
                }
                cacheMap.put(key, value);
                return value;
            }
            //2.是否超出缓存容量，超出的话就移除队列头部的元素以及其对应的缓存
            if (cacheMap.size() == maxCapacity) {
                removeOldestKey(key);
            }
            //3.key不存在于当前缓存。将key添加到队列的尾部并且缓存key及其对应的元素
            oldKeys.add(key);
            //先插入到old中，如果old满了则将淘汰出的加入到FlushList中
            if(oldKeys.size()>oldSize){
                System.out.println("1111");
                removeOldestKey(key);
            }
            cacheMap.put(key, value);
            if (expireTime > 0) {
                removeToYoungAfterExpireTime(key, expireTime);
            }
            return value;
        } finally {
            writeLock.unlock();
        }
    }
    public V get(K key) {
        //加读锁
        readLock.lock();
        try {
            //key是否存在于当前缓存
            if (cacheMap.containsKey(key)) {
                // 存在的话就将key移动到队列的尾部
                if(oldKeys.contains(key)) {
                    moveToTailOfQueue(key,oldKeys);
                }else if(youngKeys.contains(key)){
                    moveToTailOfQueue(key,youngKeys);
                }
                return cacheMap.get(key);
            }
            //不存在于当前缓存中就返回Null
            return null;
        } finally {
            readLock.unlock();
        }
    }

    public V remove(K key) {
        writeLock.lock();
        try {
            //key是否存在于当前缓存
            if (cacheMap.containsKey(key)) {
                // 存在移除队列和Map中对应的Key
                if(oldKeys.contains(key)) {
                    oldKeys.remove(key);
                }else if(youngKeys.contains(key)){
                    youngKeys.remove(key);
                }
                return cacheMap.remove(key);
            }
            //不存在于当前缓存中就返回Null
            return null;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 将元素添加到队列的尾部(put/get的时候执行)
     */
    private void moveToTailOfQueue(K key,ConcurrentLinkedQueue<K> queue) {
        queue.remove(key);
        queue.add(key);
    }

    /**
     * 移除在old中呆最久的链表
     */
    private void removeOldestKey(K key) {
        System.out.println("开始移除脏页到Flush List");
        K oldestKey = oldKeys.poll();
        flushList.offer(new AbstractMap.SimpleEntry<>(key,cacheMap.get(oldestKey)));
        if (oldestKey != null) {
            cacheMap.remove(oldestKey);
        }
    }
    //在old中存活到exprieTime的存放到youngList中
    private void removeToYoungAfterExpireTime(K key, long expireTime) {
        scheduledExecutorService.schedule(() -> {
            youngWriteLock.lock();
            System.out.println("到这里了"+key);
            if(cacheMap.get(key)!=null){
                oldKeys.remove(key);
                youngKeys.add(key);
                System.out.println("young到这里了"+key);
                while(youngKeys.size()>youngSize){
                    K poll = youngKeys.poll();
                    oldKeys.add(key);
                    //将old中淘汰出的加入到FlushList中
                    if(oldKeys.size()>oldSize){
                        removeOldestKey(key);
                    }
                }
            }
            youngWriteLock.unlock();
        }, expireTime, TimeUnit.MILLISECONDS);
    }

    public int size() {
        return cacheMap.size();
    }


    public static void main(String[] args) throws InterruptedException {
        MyLruCacheWithExpireTime<Integer,String> myLruCache = new MyLruCacheWithExpireTime<Integer, String>(10,0.5f);
        myLruCache.put(1,"Java",200);
        myLruCache.put(2,"C++",800);
        myLruCache.put(3,"Python",1500);
        System.out.println(myLruCache.size());
        System.out.println(myLruCache.youngSize);
        System.out.println(myLruCache.oldSize);
        System.out.println(myLruCache.flushSize);
        AtomicInteger i=new AtomicInteger(1);
        while(true){
            Thread.sleep(1000);
            myLruCache.put(i.getAndIncrement(),"123",1500);

            System.out.println("young:"+myLruCache.getYoungKeys().size());
            System.out.println("old:"+myLruCache.getOldKeys().size());
            System.out.println("flush:"+myLruCache.getFlushList().size());
            System.out.println("此时插入第"+i.get()+"页");
        }


    }
}