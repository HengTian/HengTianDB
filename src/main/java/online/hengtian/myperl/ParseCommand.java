package online.hengtian.myperl;

import online.hengtian.utils.MyFileUtils;

import java.util.Arrays;

import static online.hengtian.memory.DbSystem.*;
import static online.hengtian.memory.DbSystem.TABLE_DELETE;
import static online.hengtian.myperl.PrepareResult.PREPARE_SUCCESS;
import static online.hengtian.myperl.PrepareResult.PREPARE_SYNTAX_ERROR;
import static online.hengtian.myperl.PrepareResult.PREPARE_UNRECOGNIZED_STATEMENT;

public class ParseCommand {
    /**
     * 语法解析和词法解析
     * @param s
     * @param statement
     * @return
     */
    public static PrepareResult prepareStatement (String s, Statement statement){
        s=preParse(s);
        if(s==null) return PREPARE_SYNTAX_ERROR;
        if (s.contains(TABLE_SELECT)){
            statement.setType(TABLE_SELECT);
            return PREPARE_SUCCESS;
        }else if (s.contains(TABLE_INSERT)){
            //将语句写入redo.log
            MyFileUtils.writeLine(s);
            statement.setType(TABLE_INSERT);
            String[] strings = s.split(" ");
            if (strings.length!=TABLE_LINE_NUM){
                System.out.println(Arrays.toString(strings));
                return PREPARE_SYNTAX_ERROR;
            }
//            statement.setBean(o);
            return PREPARE_SUCCESS;
        }else if(s.contains(TABLE_UPDATE)){
            statement.setType(TABLE_UPDATE);
            String[] strings = s.split(" ");
            if (strings.length!=TABLE_LINE_NUM){
                System.out.println(Arrays.toString(strings));
                return PREPARE_SYNTAX_ERROR;
            }
//            statement.setBean(new User(Integer.parseInt(strings[1]),strings[2],strings[3]));
            return PREPARE_SUCCESS;
        }else if(s.contains(TABLE_DELETE)){
            statement.setType(TABLE_DELETE);
            String s1 = s.trim();
            try {
                int i = Integer.parseInt(s1);
//                statement.setBean(new User(i));
            }catch (Exception e){
                return PREPARE_SYNTAX_ERROR;
            }
        }
        return PREPARE_UNRECOGNIZED_STATEMENT;
    }
    public static String preParse(String s){
        StringBuffer sb=new StringBuffer();
        return sb.toString();
    }
}
