import java.util.ArrayList;
import java.util.LinkedList;

public class jihe_21_1 {
    public static void main(String[] args) {
        ArrayList list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        System.out.println(list);
        for(int i = 0; i < list.size(); i++)
            System.out.println(list.get(i));
        for(Object obj : list)
            System.out.println(obj);
        LinkedList linkedList = new LinkedList();
        linkedList.add(1);
        linkedList.add(2);
        linkedList.add(3);
        linkedList.add(4);
        System.out.println(linkedList);
        for(int i = 0; i < linkedList.size(); i++)
            System.out.println(linkedList.get(i));
        for(Object obj : linkedList)
            System.out.println(obj);
        System.out.println(linkedList);
        linkedList.remove(0);
        System.out.println(linkedList);
    }
}
