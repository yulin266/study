package coding.Day03;

public class class4 {
    public static void main(String[] args) {
        A.fly();
        A a = new A();
        a.run();

    }
}
class A{
    static void fly(){
        System.out.println("A can fly");}

    void run(){
        System.out.println("A can run");
        fly();
    }
}