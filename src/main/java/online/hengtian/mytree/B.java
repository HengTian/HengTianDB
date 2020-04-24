package online.hengtian.mytree;

public interface B {
    Object get(Comparable key);
    void remove(Comparable key);
    //插入或者更新，如果已经存在就更新，否则插入
    void insertOrUpdate(Comparable key,Object obj);
    void printTree();
}
