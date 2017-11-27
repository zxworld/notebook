### JDK1.8的新特性总结

#### 1、接口的定义

接口在JDK1.8之前一直都是由全局常量，抽象方法构成，而JDK1.8出现之后改变了。
可以在接口中定义普通方法，由default修饰
如：

```
    interface TestMessage{
        void test();
        default void fun(){
            System.out.println("test 123");
        }
    }
    
    class TestMessageImpl implements TestMessage{
        public void test(){
            System.out.println("test method...");
        }
    }
    
    public class TestJdk8 {
    
        public static void main(String[] args) {
            TestMessage testMessage = new TestMessageImpl();
            testMessage.fun();
      }
    
    }
    // 输出
    test 123
```

还可以定义static方法，如下：

```
    interface TestMessage{
        void test();
        default void fun(){
            System.out.println("test 123");
        }
        static void fun2(){
            System.out.println("我是interface中的static方法");
        }
    }
    
    class TestMessageImpl implements TestMessage{
        public void test(){
            System.out.println("test method...");
        }
    }
    
    public class TestJdk8 {
    
        public static void main(String[] args) {
            TestMessage testMessage = new TestMessageImpl();
        testMessage.fun();
        TestMessage.fun2();
        }
    }
    // 输出
    test 123
    我是interface中的static方法
```

#### 2、Lamda表达式

Lamda属于函数式编程的概念，要想清楚函数编程的出现原因，需要明白匿名内部类，查看如下代码：

```
    interface TestMessage {
        void test();
    }
    
    public class TestJdk8 {
    
        public static void main(String[] args) {
           print(new TestMessage() {
               @Override
               public void test() {
                   System.out.println("123456");
                   // 实际上有用的代码就这一句
               }
           });
        }
        public static void print(TestMessage testMessage){
            testMessage.test();
        }
    }
    // 输出
    123456
```

实际上有用的代码就一句，由于java语法的开发结构完整性要求，需要嵌套更多的代码。
以上的做法要求太过严谨，所以出现Lamda：

```
    interface TestMessage {
        void test();
    }
    
    public class TestJdk8 {
        public static void main(String[] args) {
           print(()-> System.out.println("123456"));
        }
        public static void print(TestMessage testMessage){
            testMessage.test();
        }
    }
    // 输出
    123456
```

Lamda语法：

```  
    三种形式
    (参数) -> 单行语句；
    (参数) -> {单行语句；单行语句}；
    (参数) -> 表达式
```

例1：

``` 
    // (参数) -> 单行语句；
    interface TestMessage {
    void test(String str);
    }
    
    public class TestJdk8 {
        public static void main(String[] args) {
           print((x)-> System.out.println(x));
        }
        public static void print(TestMessage testMessage){
            testMessage.test("123456");
        }
    }
    // 输出
    123456
```

例2：

```
    // (参数) -> {单行语句；单行语句}；
    interface TestMessage {
    void test(String str);
    }
    
    public class TestJdk8 {
        public static void main(String[] args) {
           print((x)-> {
               x = x.toUpperCase();
               System.out.println(x); });
        }
        public static void print(TestMessage testMessage){
            testMessage.test("abcd");
        }
    }
```

例3：

```
    interface TestMessage {
        int test(int x, int y);
    }
    
    public class TestJdk8 {
        public static void main(String[] args) {
            print((x, y) -> x + y);
        }
    
        public static void print(TestMessage testMessage) {
            System.out.println(testMessage.test(1, 2));
        }
    }
    // 可以写return
    print((x, y) -> {
            return x + y;
        });
    // 当只有一个表示式的时候一般不写return,像上面那样写就行了..
    
    // 输出
    3
```

#### 3、方法引用

一直以来都只是能在对象上发现引用，在java8中出现方法引用。
方法引用一共定义了4种形式：

```
    引用静态方法：className :: staticMethodName;
    引用普通方法：实例化对象 :: methodName // 普通方法;
    引用特定类型的方法：className :: methodName;
    引用构造方法：className :: new;
```

例1：

```
    // 引用静态方法：className :: staticMethodName;
    interface TestMessage<T, R> {
    R test(T t);
    }
    
    public class TestJdk8 {
        public static void main(String[] args) {
            // 将string中的valueOf方法变为了TestMessage中的test方法
            TestMessage<Integer, String> testMessage = String::valueOf;
            String s = testMessage.test(8);
            System.out.println(s);
        }
    }
    // 输出
    8
```

例2：

```
    // 引用普通方法：实例化对象 :: methodName // 普通方法;
    interface TestMessage<R> {
    R test();
    }
    
    public class TestJdk8 {
        public static void main(String[] args) {
            // 将toUpperCase的引用交给了TestMessage的test方法
            TestMessage<String> testMessage = "abcd"::toUpperCase;
            String s = testMessage.test();
            System.out.println(s);
        }
    }
    // 输出
    ABCD
```

<font color=red>可以看出方法引用需要一个接口，接口里面也只能有一个方法，所以为了限制这样的接口，提供了一个注解来标识这样的函数式接口<label>@FunctionalInterface</label></font>

例3：

```
    // 引用特定类型的方法：className :: methodName;
    @FunctionalInterface
    interface TestMessage<T> {
        int test(T t, T t1);
    }
    
    public class TestJdk8 {
        public static void main(String[] args) {
            TestMessage<String> testMessage = String :: compareTo;
            System.out.println(testMessage.test("A","B"));
    
        }
    }
    // 输出
    -1
```

例4：

```
    // 引用构造方法：className :: new;
    @FunctionalInterface
    interface TestMessage<T> {
        T test(String name, double price);
    }
    class Book{
        private String name;
        private double price;
    
        public Book(String name, double price) {
            this.price = price;
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    
        public double getPrice() {
            return price;
        }
        public void setPrice(double price) {
            this.price = price;
        }
        @Override
        public String toString() {
            return "Book{" +
                    "name='" + name + '\'' +
                    ", price=" + price +
                    '}';
        }
    }
    public class TestJdk8 {
        public static void main(String[] args) {
            TestMessage<Book> testMessage = Book :: new;
            Book book = testMessage.test("123", 21.2);
            System.out.println(book);
        }
    }
    // 输出
    Book{name='123', price=21.2}
``` 
    
<font color=green>java8中提供的函数接口</font>
