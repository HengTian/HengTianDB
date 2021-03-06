package online.hengtian.mytree;

import org.omg.IOP.ENCODING_CDR_ENCAPS;

import java.util.Map;
import java.util.Random;

public class BPlusTree implements B {
    protected Node root;
    protected Node head;
    /**
     * B树的阶数M
     */
    protected int order;
    /**
     * B树叶子节点的数据长度L
     */
    protected int length;
    public BPlusTree(int order, int length) {
        if (order < 3) {
            System.out.print("order must be greater than 2");
            System.exit(0);
        }
        this.order = order;
        this.length = length;
        root = new Node(true, true);
        head = root;
    }
    @Override
    public Object get(Comparable key) {
        return root.get(key);
    }

    @Override
    public void remove(Comparable key) {

    }

    @Override
    public void insertOrUpdate(Comparable key, Object obj) {
        root.insertOrUpdate(key,obj,this);
    }
    public BPlusTree(int order){
        if (order < 3) {
            System.out.print("order must be greater than 2");
            System.exit(0);
        }
        this.order = order;
        this.length=order;
        root = new Node(true, true);
        head = root;
    }


    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public Node getHead() {
        return head;
    }

    public void setHead(Node head) {
        this.head = head;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public static void main(String[] args) {
        BPlusTree tree = new BPlusTree(6);
        Random random = new Random();
        long current = System.currentTimeMillis();
        for (int j = 0; j < 100000; j++) {
            for (int i = 0; i < 100; i++) {
                int randomNumber = random.nextInt(100000);
                tree.insertOrUpdate(randomNumber, randomNumber);
            }
        }

        long duration = System.currentTimeMillis() - current;
        System.out.println("插入耗时: " + duration+" ms ");
        Node head=tree.getHead();
        while(head.next!=null) {
            for (Map.Entry entry : head.getEntries()) {
                System.out.print(entry.getValue() + " ");
            }
            head=head.next;
        }
        //int search = 80;
        //System.out.print(tree.get(search));
    }


}
