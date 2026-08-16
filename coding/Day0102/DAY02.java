package coding.Day0102;

public class DAY02 {
    public static void main(String[] args) {
//      TODO 尝试
//        int i = 1;
//        i++;
//        System.out.println(i);
    //TODO 练习分支
//        int age =30;
//        if(age>=18){
//            System.out.print("adult");
//        }else{
//            System.out.println("child");
//        }
// TODO JIUCENGYAOTA
//        for (int i = 0; i < 3; i++) {
//            System.out.print("*");
//        }
//        System.out.println(" ");
//        for (int i = 0; i < 5; i++) {
//            System.out.print("*");
//        }
//        System.out.println(" ");
//        for (int i = 0; i < 7; i++) {
//            System.out.print("*");
//        }
//        System.out.println(" ");
        int level =  9;
        for (int j = 0; j < level; j++) {
        for (int k = 0; k < level - j; k++) {
            System.out.print(" ");
        }
        for (int i = 0; i < j*2+1; i++) {
            System.out.print("*");
        }
        System.out.println(" ");
//
    }
  }

}