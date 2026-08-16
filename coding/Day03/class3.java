package coding.Day03;

public class class3 {
    public static void main(String[] args) {
        user07 a = new user07();
        a.say("小王", 18);
        user03 b = new user03();
        b.say("小王","xiao","zzz");
    }
}
 class user07{
    void say(String name ,int age ){
        System.out.println("姓名："+name+"年龄："+age);
    }

}
class user03{
    void say(String...name ){
        System.out.println(name);
    }
}