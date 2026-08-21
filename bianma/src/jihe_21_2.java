public class jihe_20_2 {
    public static void main(String[] args) {
        java.util.ArrayList list = new java.util.ArrayList<>();
        Person person = new Person();
        User20 user20 = new User20();
        list.add(person);
        list.add(user20);
        Object obj = list.get(0);
        if (obj instanceof Person){
        Person person1 = (Person) obj;
            person1.testPerson();
        } else if (obj instanceof User20) {
            User20 user20 = (User20) obj;
            user20.testUser20();
        }

    }
}
class Person{
    public void testPerson(){
        System.out.println("Person testPerson");
    }
}
class User20{
    public void testUser20(){
        System.out.println("User20 testUser20");
    }
}