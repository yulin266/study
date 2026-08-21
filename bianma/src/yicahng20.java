public class yicahng20{
public static void main(String[] args){
    int i = 0;
    int j = 10;
    try {
        j = 10/i;
    }catch (ArithmeticException e){
        System.out.println(e.getMessage());
        System.out.println(e.getCause())    ;
e.printStackTrace();
    }finally{
        System.out.println("finally");
    }

}
}



