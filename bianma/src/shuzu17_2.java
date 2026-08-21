public class shuzu17_2 {
    public static void main(String[] args) {//TODO 选择排序


        int [] arr = {1,4,3,5,2};
        for (int i = 0; i < arr.length - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[j] < arr[minIndex]) {
                    minIndex = j;
                }
            }
            int temp = arr[i];
            arr[i] = arr[minIndex];
            arr[minIndex] = temp;
        }
        for (int i : arr) {
            System.out.print(i + " ");
        }
    }
}
