public class jiekou13 {
    public static void main(String[] args) {
        Computer c= new Computer();
        Light light = new Light();
        Light light2 = new Light();
        c.usb1 = light;
        c.usb2 = light2;
       c.supply();
    }
}
interface  USB {

}
interface  Usbsupply extends USB{
    public void supply();
}
interface  Usbreceive extends USB{
    public void receive();
}
class  Computer implements Usbsupply{
    public  Usbreceive usb1;
    public   Usbreceive usb2;

    public void supply() {
        System.out.println("supply");

//        usb1.receive();
//        usb2.receive();
    }


}
class Light implements Usbreceive{
    public void receive() {
        System.out.println("light receive");
    }
}










