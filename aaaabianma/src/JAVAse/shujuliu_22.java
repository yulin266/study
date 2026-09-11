package aaaabianma.src.JAVAse;

import java.io.File;
public class shujuliu_22 {
    public static void main(String[] args) {
        String filepath = "D:\\study-demo\\bianma\\Data\\word.txt";
        File file = new File(filepath);//文件的路经
        System.out.println(file);
        System.out.println(file.isFile());
        if(file.exists()){
            System.out.println("文件存在guanlian");
            if(file.isFile())
            System.out.println("文件存在");

            else if (file.isDirectory()) {
                System.out.println("文件夹存在");
            }
        }else {
            System.out.println("文件不存在");
            file.mkdirs();
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
