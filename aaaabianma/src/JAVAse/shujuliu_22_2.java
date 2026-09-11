package aaaabianma.src.JAVAse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.BufferedOutputStream;
public class shujuliu_22_2 {
    public static void main(String[] args){
        FileInputStream fis = null;
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        byte [] buffer = new byte[1024];//缓冲区

        File srcfile = new File("D:\\study-demo\\bianma\\Data\\word.txt");
        File destfile = new File("D:\\study-demo\\bianma\\Data\\word_copy.txt");
        try {
            fis = new FileInputStream(srcfile);
            fos = new FileOutputStream(destfile);
            bis = new BufferedInputStream(fis);//对接的管道
            bos = new BufferedOutputStream(fos);
            int data = fis.read();
            while ((data = bis.read()) != -1) {
                bos.write(data);
                bos.write(buffer, 0, data);
                bos.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
               if(bis != null)
                bis.close();
               if(bos != null)
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
