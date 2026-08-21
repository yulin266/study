import java.util.Random;
import java.util.UUID;

public class qitalei19 {
    public static void main(String[] args) {
        System.out.println(StringUtil.isEmpty("")); //TODO 测试
    }

}
class StringUtil{
        public static boolean isEmpty(String str) {
            if(str == null || str.length() == 0
                    || str.trim().length() == 0
            )
                return true;
            else
                return false;
        }
        public static boolean isNotEmpty(String str) {
            return !isEmpty(str);
        }
        //生成随机字符串
    public static String makeString() {
            return UUID.randomUUID().toString();
    }
    public static String makeString(String from,int length) {
            if(length <= 0){
                return "";
    }else {
                char[] chars = from.toCharArray();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < length; i++){
                    Random random = new Random();

                    int j = random.nextInt(chars.length);
                    char c = chars[j];

                    sb.append(c);
                }
                return sb.toString();
            }


            }


}
