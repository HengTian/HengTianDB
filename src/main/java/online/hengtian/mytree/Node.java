package online.hengtian.mytree;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Node {
    //是否为叶子节点
    protected boolean isLeaf;
    //是否为根节点
    protected boolean isRoot;
    protected Node parent;
    protected Node previous;
    protected Node next;
    //如果是叶子节点，保存的就是以key为关键字，obj为值的数据，obj代表一行类似
    protected List<Map.Entry<Comparable,Object>> entries;
    //如果不是叶子节点，保存的就是索引
    protected List<Node> children;

    public Node(boolean isLeaf) {
        this.isLeaf=isLeaf;
        entries=new ArrayList<Map.Entry<Comparable, Object>>();
        if(!isLeaf){
            children=new ArrayList<Node>();
        }
    }

    public Node(boolean isLeaf, boolean isRoot) {
        this(isLeaf);
        this.isRoot = isRoot;
    }
    public Object get(Comparable key){
        if(isLeaf){
            for(Map.Entry<Comparable,Object> entry:entries){
                if(entry.getKey().compareTo(key)==0){
                    return entry.getValue();
                }
            }
            return null;
        }else {
            if(key.compareTo(entries.get(0).getKey())<=0){
                return children.get(0).get(key);
            }else if(key.compareTo(entries.get(entries.size()-1).getKey())>=0){
                return children.get(entries.size()-1).get(key);
            }else{
//                遍历1~size-1的节点
                for(int i=0;i<entries.size();i++){
                    //找到关键字
                    if(entries.get(i).getKey().compareTo(key) <= 0 && entries.get(i+1).getKey().compareTo(key) > 0){
                        return children.get(i).get(key);
                    }
                }
            }
        }
        return null;
    }
    //如果包含该key就更新，如果不包含就插入
    //后期要改成可以支持有相同节点的
    public void insertOrUpdate(Comparable key,Object obj,BPlusTree tree){
        //如果是叶子节点
        if(isLeaf){
            //直接插入，不需要分裂
            if(contains(key)||entries.size()<tree.getLength()){
                insertOrUpdate(key,obj);
                if(parent!=null){
                    parent.updateInsert(tree);
                }
            }else{
                Node left=new Node(true);
                Node right=new Node(true);
                if(previous!=null){
                    previous.setNext(left);
                    left.setPrevious(previous);
                }
                if(next!=null){
                    next.setPrevious(right);
                    right.setNext(next);
                }
                if(previous==null){
                    tree.setHead(left);
                }
                left.setNext(right);
                right.setPrevious(left);
                previous=null;
                next=null;
                //这里的参数tree.getOrder如果改成别的可以控制数据项的个数
                //todo
                int leftSize=(tree.getLength()+1)/2+(tree.getLength()+1)%2;
                int rightSize=(tree.getLength()+1)/2;
                //将要放入的节点加入到原节点的数据列中
                //先放后分，先分后放的话还需要判断应该放在左边还是右边
                insertOrUpdate(key,obj);
                for(int i=0;i<leftSize;i++){
                    left.getEntries().add(entries.get(i));
                }
                for(int i=0;i<rightSize;i++){
                    right.getEntries().add(entries.get(i+leftSize));
                }

                if(parent!=null){
                    int index=parent.getChildren().indexOf(this);
                    parent.getChildren().remove(this);
                    left.setParent(parent);
                    right.setParent(parent);
                    parent.getChildren().add(index,left);
                    parent.getChildren().add(index+1,right);
                    setEntries(null);
                    setChildren(null);
                    //父节点更新关键字
                    parent.updateInsert(tree);
                    setParent(null);
                }else{
                    isRoot=false;
                    Node parent = new Node(false, true);
                    tree.setRoot(parent);
                    left.setParent(parent);
                    right.setParent(parent);
                    parent.getChildren().add(left);
                    parent.getChildren().add(right);
                    setEntries(null);
                    setChildren(null);

                    //更新根节点
                    parent.updateInsert(tree);
                }
            }
        }else{
            //todo
            if(key.compareTo(entries.get(0).getKey())<=0){
                children.get(0).insertOrUpdate(key,obj,tree);
            }else if(key.compareTo(entries.get(entries.size()-1).getKey())>=0){
                children.get(children.size()-1).insertOrUpdate(key,obj,tree);
            }else{
                for(int i=0;i<entries.size();i++){
                    if (entries.get(i).getKey().compareTo(key) <= 0 && entries.get(i+1).getKey().compareTo(key) > 0) {
                        children.get(i).insertOrUpdate(key, obj, tree);
                        break;
                    }
                }
            }
        }
    }
    public void updateInsert(BPlusTree tree){
        validate(this,tree);
        //如果插入后子节点数超出阶数，则需要分裂该节点
        if(children.size()>tree.getOrder()){
            //声明左右节点
            Node left=new Node(false);
            Node right=new Node(false);

            int leftSize=(tree.getOrder()+1)/2+(tree.getOrder()+1)%2;
            int rightSize=(tree.getOrder()+1)/2;
            //均摊
            for(int i=0;i<leftSize;i++){
                left.getChildren().add(children.get(i));
                left.getEntries().add(new AbstractMap.SimpleEntry<Comparable, Object>(children.get(i).getEntries().get(0).getKey(),null));
                children.get(i).setParent(left);
            }
            for(int i=0;i<rightSize;i++){
                right.getChildren().add(children.get(i+leftSize));
                right.getEntries().add(new AbstractMap.SimpleEntry<Comparable, Object>(children.get(i+leftSize).getEntries().get(0).getKey(),null));
                children.get(i+leftSize).setParent(right);
            }
            //如果不是根节点
            if(parent!=null){
                int index=parent.getChildren().indexOf(this);
                parent.getChildren().remove(this);
                left.setParent(parent);
                right.setParent(parent);
                parent.getChildren().add(index,left);
                parent.getChildren().add(index+1,right);
                //父节点更新索引
                parent.updateInsert(tree);
                //回收分裂之前的内存
                setEntries(null);
                setChildren(null);
                setParent(null);

            }
            //如果是根节点
            else{
                isRoot=false;
                Node parent=new Node(false,true);
                tree.setRoot(parent);
                left.setParent(parent);
                right.setParent(parent);
                parent.getChildren().add(left);
                parent.getChildren().add(right);
                //父节点更新索引
                parent.updateInsert(tree);
                //回收分裂之前的内存
                setEntries(null);
                setChildren(null);
            }

        }

    }

    //调整（更新）索引节点（非叶子节点）的关键字
    protected void validate(Node node,BPlusTree tree){
        //如果关键字个数与子节点个数相同
        //首先此时是非叶子节点
        if(node.getEntries().size()==node.getChildren().size()){
            for(int i=0;i<node.getEntries().size();i++){
                //获取数据节点中列表的第一个元素
                Comparable key=node.getChildren().get(i).getEntries().get(0).getKey();
                //如果索引节点上的数据和数据节点中第一个元素的值不相同，就替换
                if(node.getEntries().get(i).getKey().compareTo(key)!=0){
                    node.getEntries().remove(i);
                    node.getEntries().add(i,new AbstractMap.SimpleEntry<>(key,null));
                    //依次更新父节点
                    if(!node.isRoot()){
                        validate(node.getParent(),tree);
                    }
                }
            }
        }
        //子节点（有可能是根节点）满足规则
        else if((node.isRoot()&&node.getChildren().size()>=2)
        ||(node.getChildren().size()>=tree.getOrder()/2
        &&node.getChildren().size()<=tree.getOrder()
        &&node.getChildren().size()>=2)){
            node.getEntries().clear();
            for(int i=0;i<node.getChildren().size();i++){
                Comparable key=node.getChildren().get(i).getEntries().get(0).getKey();
                node.getEntries().add(new AbstractMap.SimpleEntry<>(key,null));
                if(!node.isRoot){
                    validate(node.getParent(),tree);
                }
            }
        }
    }



    //在一个叶节点的数据未满的时候插入或者更新
    public void insertOrUpdate(Comparable key, Object obj) {
        Map.Entry<Comparable,Object> entry=new AbstractMap.SimpleEntry<Comparable, Object>(key,obj);
        //如果关键字列表长度为0，则直接插入
        if(entries.size()==0){
            entries.add(entry);
            return;
        }
        //遍历改列表
        for(int i=0;i<entries.size();i++){
            //如果关键字存在，就更新
            if(entries.get(i).getKey().compareTo(key)==0){
                entries.get(i).setValue(obj);
                return;
            }else if(entries.get(i).getKey().compareTo(key)>0){
                if(i==0){
                    entries.add(0,entry);
                    return;
                }else {
                    entries.add(i,entry);
                    return;
                }
            }
        }
        //插入到末尾
        entries.add(entries.size(),entry);
    }

    //判断当前节点是否包含该关键字
    protected boolean contains(Comparable key) {
        for (Map.Entry<Comparable, Object> entry : entries) {
            if (entry.getKey().compareTo(key) == 0) {
                return true;
            }
        }
        return false;
    }

    public void remove(Comparable key,BPlusTree tree){
        if(isLeaf){
            if(!contains(key)){
                return;
            }
            if(isRoot){
                remove(key);
            }
            if(entries.size()>tree.getLength()/2&&entries.size()>2){
                remove(key);
            }else{
                if(previous!=null
                        &&previous.getEntries().size()>tree.getLength()/2
                        &&previous.getEntries().size()>2
                        &&previous.getParent()==parent){
                    int size=previous.getEntries().size();
                    Map.Entry<Comparable, Object> entry = previous.getEntries().get(size - 1);
                    previous.getEntries().remove(entry);
                    entries.add(0,entry);
                    remove(key);
                }else if(next!=null
                    && next.getEntries().size()>tree.getLength()/2
                    && next.getEntries().size()>2
                    && next.getParent()==parent){
                    Map.Entry<Comparable, Object> entry = next.getEntries().get(0);
                    next.getEntries().remove(entry);
                    entries.add(entry);
                    remove(key);
                }else{
                    if(previous!=null
                        &&(previous.getEntries().size()<=tree.getLength()/2||previous.getEntries().size()>2)
                        &&previous.getParent()==parent){
                        for (int i = previous.getEntries().size() - 1; i >=0; i--) {
                            //从末尾开始添加到首位
                            entries.add(0, previous.getEntries().get(i));
                        }
                        remove(key);
                        previous.setParent(null);
                        previous.setEntries(null);
                        parent.getChildren().remove(previous);
                        //更新链表
                        if (previous.getPrevious() != null) {
                            Node temp = previous;
                            temp.getPrevious().setNext(this);
                            previous = temp.getPrevious();
                            temp.setPrevious(null);
                            temp.setNext(null);
                        }else {
                            tree.setHead(this);
                            previous.setNext(null);
                            previous = null;
                        }
                    }else if(next != null
                            && (next.getEntries().size() <= tree.getLength() / 2 || next.getEntries().size() <= 2)
                            && next.getParent() == parent){
                        for (int i = 0; i < next.getEntries().size(); i++) {
                            //从首位开始添加到末尾
                            entries.add(next.getEntries().get(i));
                        }
                        remove(key);
                        next.setParent(null);
                        next.setEntries(null);
                        parent.getChildren().remove(next);
                        //更新链表
                        if (next.getNext() != null) {
                            Node temp = next;
                            temp.getNext().setPrevious(this);
                            next = temp.getNext();
                            temp.setPrevious(null);
                            temp.setNext(null);
                        }else {
                            next.setPrevious(null);
                            next = null;
                        }
                    }
                }
                parent.updateRemove(tree);
            }
            //如果不是叶子节点   
        }else {
            //如果key小于等于节点最左边的key，沿第一个子节点继续搜索 
            if (key.compareTo(entries.get(0).getKey()) <= 0) {
                children.get(0).remove(key, tree);
                //如果key大于节点最右边的key，沿最后一个子节点继续搜索 
            }else if (key.compareTo(entries.get(entries.size()-1).getKey()) >= 0) {
                children.get(children.size()-1).remove(key, tree);
                //否则沿比key大的前一个子节点继续搜索 
            }else {
                for (int i = 0; i < entries.size(); i++) {
                    if (entries.get(i).getKey().compareTo(key) <= 0 && entries.get(i+1).getKey().compareTo(key) > 0) {
                        children.get(i).remove(key, tree);
                        break;
                    }
                }
            }
        }
    }

    /** 删除节点后中间节点的更新*/
    protected void updateRemove(BPlusTree tree) {
        validate(this, tree);
        // 如果子节点数小于M / 2或者小于2，则需要合并节点
        if (children.size() < tree.getOrder() / 2 || children.size() < 2) {
            if (isRoot) {
                // 如果是根节点并且子节点数大于等于2，OK
                if (children.size() >= 2) {
                    return;
                    // 否则与子节点合并
                } else {
                    Node root = children.get(0);
                    tree.setRoot(root);
                    root.setParent(null);
                    root.setRoot(true);
                    setEntries(null);
                    setChildren(null);
                }
            } else {
                //计算前后节点
                int currIdx = parent.getChildren().indexOf(this);
                int prevIdx = currIdx - 1;
                int nextIdx = currIdx + 1;
                Node previous = null, next = null;
                if (prevIdx >= 0) {
                    previous = parent.getChildren().get(prevIdx);
                }
                if (nextIdx < parent.getChildren().size()) {
                    next = parent.getChildren().get(nextIdx);
                }

                // 如果前节点子节点数大于M / 2并且大于2，则从其处借补
                if (previous != null
                        && previous.getChildren().size() > tree.getOrder() / 2
                        && previous.getChildren().size() > 2) {
                    //前叶子节点末尾节点添加到首位
                    int idx = previous.getChildren().size() - 1;
                    Node borrow = previous.getChildren().get(idx);
                    previous.getChildren().remove(idx);
                    borrow.setParent(this);
                    children.add(0, borrow);
                    validate(previous, tree);
                    validate(this, tree);
                    parent.updateRemove(tree);

                    // 如果后节点子节点数大于M / 2并且大于2，则从其处借补
                } else if (next != null
                        && next.getChildren().size() > tree.getOrder() / 2
                        && next.getChildren().size() > 2) {
                    //后叶子节点首位添加到末尾
                    Node borrow = next.getChildren().get(0);
                    next.getChildren().remove(0);
                    borrow.setParent(this);
                    children.add(borrow);
                    validate(next, tree);
                    validate(this, tree);
                    parent.updateRemove(tree);

                    // 否则需要合并节点
                } else {
                    // 同前面节点合并
                    if (previous != null
                            && (previous.getChildren().size() <= tree.getOrder() / 2 || previous.getChildren().size() <= 2)) {

                        for (int i = previous.getChildren().size() - 1; i >= 0; i--) {
                            Node child = previous.getChildren().get(i);
                            children.add(0, child);
                            child.setParent(this);
                        }
                        previous.setChildren(null);
                        previous.setEntries(null);
                        previous.setParent(null);
                        parent.getChildren().remove(previous);
                        validate(this, tree);
                        parent.updateRemove(tree);

                        // 同后面节点合并
                    } else if (next != null
                            && (next.getChildren().size() <= tree.getOrder() / 2 || next.getChildren().size() <= 2)) {

                        for (int i = 0; i < next.getChildren().size(); i++) {
                            Node child = next.getChildren().get(i);
                            children.add(child);
                            child.setParent(this);
                        }
                        next.setChildren(null);
                        next.setEntries(null);
                        next.setParent(null);
                        parent.getChildren().remove(next);
                        validate(this, tree);
                        parent.updateRemove(tree);
                    }
                }
            }
        }
    }


    //删除叶子节点中的数据
    protected void remove(Comparable key){
        int index=-1;
        for(int i=0;i<entries.size();i++){
            if(entries.get(i).getKey().compareTo(key)==0){
                index=i;
                break;
            }
        }
        if(index!=-1){
            entries.remove(index);
        }
    }



    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getPrevious() {
        return previous;
    }

    public void setPrevious(Node previous) {
        this.previous = previous;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public List<Map.Entry<Comparable, Object>> getEntries() {
        return entries;
    }

    public void setEntries(List<Map.Entry<Comparable, Object>> entries) {
        this.entries = entries;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }


    //toString()
    //todo
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("isRoot: ");
        sb.append(isRoot);
        sb.append(", ");
        sb.append("isLeaf: ");
        sb.append(isLeaf);
        sb.append(", ");
        sb.append("keys: ");
        for (Map.Entry entry : entries){
            sb.append(entry.getKey());
            sb.append(", ");
        }
        sb.append(", ");
        return sb.toString();

    }
}
