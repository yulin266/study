package coding.Day03;

public class class1 {
    public static void main(String[] args) {
        Animal a = new Animal();
        a.type = "狗";
        a.name = "旺财";
        a.eat();
    }
}
    class Animal {
        String type;
        String name;
        void eat() {
            System.out.println("吃");
        }
    }


