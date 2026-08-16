package coding.Day03;

public class class2 {
    public static void main(String[] args) {
        game a = new game();

        boolean b  = a.register();
        boolean c = a.login();
        if(b){
            if(c){
                System.out.println("登录成功");
            }else{
                System.out.println("登录失败");
            }}
     else{
                System.out.println("注册失败");
            }


}
static class game {
    boolean register(){
        System.out.println("sucess");
        return true;
    }
    boolean login() {
        System.out.println("sucess");
        return false;

    }
    }}

