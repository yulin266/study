package aaaabianma.src.JAVAse;

public class shuzu17 {
    public static void main(String[] args) {
        int [] arr = {1,4,3,5,2};// 冒泡排序
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }


        }

        for (int i : arr) {
            System.out.print(i + " ");
        }
        //写入新的数组咋整
        int [] newArr = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            newArr[i] = arr[i];
        }

    }
}

