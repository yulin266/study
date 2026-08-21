public class niminglei14 {
    public static void main(String[] args) {
        Me me = new Me();
        me.say(new Person14() {
            @Override
            public String name() {
                return "zhangsan";
            }
        });
        me.say(new Zhangsan());
    }
}
class Me{
   public void say(Person14 person14){
        System.out.println("hello"+person14.name());
    }
}
abstract  class Person14{
    public abstract  String name();

}
class Zhangsan extends Person14{
    @Override
    public String name() {
        return "zhangsan";
    }
}


