package online.hengtian.utils;

public class MyStringUtils {
    /**
     * 将数据库的字段名变成驼峰命名法
     * @param name
     * @return
     * @throws Exception
     */
    public static String getCamelCase(String name,boolean isTable){
        boolean isUpper=false;
        StringBuffer sb=new StringBuffer();
        if(name==null||name.length()==0){
            try {
                throw new Exception("数据库字段名转换错误");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        name = name.toLowerCase();
        if(isTable){
            isUpper=true;
        }
        for(int i=0;i<name.length();i++){
            if(isUpper){
                sb.append((char)(name.charAt(i)-'a'+'A'));
                isUpper=false;
            }else if(name.charAt(i)!='_'){
                sb.append(name.charAt(i));
            }
            if(name.charAt(i)=='_'){
                isUpper=true;
            }
        }
        return sb.toString();
    }

    /**
     * 检查命名规范
     * 如：email int
     * @param name
     * @return
     */
    public boolean checkName(String name){
        return true;
    }
}
