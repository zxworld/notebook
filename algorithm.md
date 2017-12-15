### 随记一些算法

> 二分搜索

```
思路: 
	每次取数组的1/2的值来对比目标值.
	有两个指针,初始化头指针在0,尾指针在数组长度上.
	循环头指针小于等于尾指针.
	取头指针加上二分之一尾指针减去头指针的值.对比值.
	比目标值小:移动头指针到当前取的值的位置.再对比.
	比目标值大:移动尾指针到当前取的值的位置.再对比.

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
// 数据结构
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

> 选择排序

```
思路:
	依次循环数组位置
	每次循环找最小的那个值,进行当前位置的替换
	
public static Comparable[] sort(Comparable[] a) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int min = i;
            for (int j = i + 1; j < N; j++) {
                if (a[min].compareTo(a[j]) > 0) {
                    Comparable tmp = a[min];
                    a[min] = a[j];
                    a[j] = tmp;
                    min = j;
                }
            }
        }
        return a;
    }
``` 

> 插入排序

```
int[] a = new int[5];
a[0] = 12;
a[1] = 45;
a[2] = 108;
a[3] = 1;
a[4] = 3;
for (int i = 1; i < a.length; i++) {
   int insert = a[i];
   int j = i - 1;
   while (j >= 0 && insert < a[j]) {
      a[j + 1] = a[j];
      j--;
   }
   a[j + 1] = insert;
}
System.out.println(Arrays.toString(a));

```