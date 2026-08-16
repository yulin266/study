package coding.Day03;

public class class7 {
    public static void main(String[] args) {
         Child2 child = new Child2("child");
    }

    public  static class Parent2 {
        String oldname;

        Parent2 (String name) {
            this.oldname = name;
        }

        void test() {
            System.out.println("test");
        }
    }
}
      class Child2 extends class7.Parent2 {
        String newname;
        Child2(String name){
            super("chil");
            this.newname = name;
            System.out.println(newname);
        }
        void test(){
            System.out.println("test");
        }
    }

