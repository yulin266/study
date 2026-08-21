public class yicahng20_2 {
    //自定义登陆异常
    public static void main(String[] args)throws Exception {
        String account = "zhangsan";
        String password = "111111";
        try {
        login(account, password);
        } catch (AccountException | PasswordException e) {
            e.printStackTrace();
        }

}
public static void login(String account , String password){
        if(!account.equals("admin")) {
            throw new AccountException("账号异常");
        }
        if(!password.equals("123456")) {
            throw new PasswordException("密码异常");
        }
        System.out.println("登陆成功");
}
}
class AccountException extends LoginException{
    public AccountException(String message) {
        super(message);
    }
}
class PasswordException extends LoginException {
    public PasswordException(String message) {
        super(message);
    }
}


class LoginException extends RuntimeException {
    public LoginException(String message) {
        super(message);
    }
}

