public class neibulei {
    public static void main(String[] args) {
        Out out = new Out();
        Out.In in = out.new In();
        in.show();

    }
}
 class Out {
    public  class In {
        public void show() {
            System.out.println("Inner class");
        }
    }

}