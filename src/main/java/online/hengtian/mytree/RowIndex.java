package online.hengtian.mytree;

/**
 * 返回的RowIndex中附带了Node与该叶子节点所在的下标，便于定位
 * @author <a href="lzh@sq108.com">陆子恒</a>
 * @date 2020/4/24 14:42
 */
public class RowIndex {
    private Node node;
    private Integer index;

    public RowIndex(Node node, Integer index) {
        this.node = node;
        this.index = index;
    }
    public Object getValue(){
        return node.entries.get(index).getValue();
    }
    public int getRowNum(){
        int num=index;
        Node tmp=node;
        while(tmp.previous!=null){
            tmp=tmp.previous;
            num+=tmp.getEntries().size();
        }
        return num;
    }
    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}
