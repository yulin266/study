public class danlimoshi11 {

    public static void main(String[] args) {
        User11 user11 = User11.getInstance();
        User11 user11_1 = User11.getInstance();
        System.out.println(user11 == user11_1);
    }
}
class User11 {
    private static User11 user11 = null;//这是静态变量才能被下边的getInstance()静态方法调用
    private User11() {//外部无法new对象

    }
    public static User11 getInstance() {
        if (user11 == null) {
            user11 = new User11();
        }
        return user11;
    }
//    public User11 getUser11() {
//        if (user11 == null) {
//            user11 = new User11();
//        }
//        return user11;
//    }

}
