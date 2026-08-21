public class xiancheng23 {
    public static void main(String[] args) {
        System.out.println("Main thread is running");
//       Thread t = new Thread();
        MyThread t = new MyThread();
        t.start();

    }
}
class MyThread extends Thread {


    public void run() {
        System.out.println("Thread is running");
    }
}
