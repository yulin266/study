package coding.Day03;

public class class6 {
    public static void main(String[] args) {
        Child child = new Child();
        System.out.println(child.name);
        child.test();
    }
}

class Parent{
    String name = "zhangsan";
    void test(){
        System.out.println("test1");
    }
}
class Child extends Parent {
//    String name = "lisi";
//
//    void test() {
//        System.out.println("test2");
 //   }
}

