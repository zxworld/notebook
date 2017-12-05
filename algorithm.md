### 随记一些算法

> 二分搜索

```
public static int rank(int key, int[] a) {
        int lo = 0;
        int hi = a.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (key < a[mid]) {
                hi = mid - 1;
            } else if (key > a[mid]) {
                lo = mid + 1;
            } else {
                return mid;
            }
        }
        return -1;
    }
```

> 单向链表反转

```
数据结构
class Node {
    private int  data;
    private Node next;

    public Node(int data) {
        this.data = data;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }
}

public class TestMain {

    public static void main(String[] args) {
        Node head = new Node(0);
        Node node1 = new Node(1);
        Node node2 = new Node(2);
        Node node3 = new Node(3);
        head.setNext(node1);
        node1.setNext(node2);
        node2.setNext(node3);

        Node h = head;
        while (h != null) {
            System.out.println(h.getData());
            h = h.getNext();
        }

        // 非递归反转法
        // Node reverse = reverse(head);
        
        // 递归反转法
        Node reverse = reverse2(head);

        while (reverse != null) {
            System.out.println(reverse.getData());
            reverse = reverse.getNext();
        }
    }

    private static Node reverse(Node node) {
        Node prev = null;
        while (node != null) {
            Node tmp = node.getNext();
            node.setNext(prev);
            prev = node;
            node = tmp;
        }
        return prev;
    }

    private static Node reverse2(Node node) {
        if (node == null || node.getNext() == null) {
            return node;
        }
        Node prev = reverse2(node.getNext());
        node.getNext().setNext(node);
        node.setNext(null);
        return prev;
    }


}
```