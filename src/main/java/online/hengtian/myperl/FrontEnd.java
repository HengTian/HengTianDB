package online.hengtian.myperl;

import online.hengtian.memory.DbSystem;
import online.hengtian.memory.Page;
import online.hengtian.memory.Table;
import online.hengtian.mytree.BPlusTree;
import online.hengtian.table.User;
import online.hengtian.utils.MyFileUtils;
import online.hengtian.utils.SerializeUtils;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static online.hengtian.memory.DbSystem.*;
import static online.hengtian.myperl.ExcuteResult.*;
import static online.hengtian.myperl.PrepareResult.*;

//后面可以换成JLine框架实现自动补全等命令行常用功能

/**
 * @author 陆子恒
 * @date 2020.4.14
 */
public class FrontEnd {
    public final static File redoLog=new File("redo.log");

    public static void main(String[] args){
        printPromptFront();//打印提示内容
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        boolean isExit=true;
        Table table=new Table().dbOpen("user");
        while(isExit){
            printPrompt();
            String s = null;
            Statement statement=new Statement();
            try {
                s = br.readLine();
                if (!"exit".equals(s)){
                    switch (prepareStatement(s,statement)){
                        case PREPARE_SUCCESS:
                            System.out.println(statement.getType()+" success");
                            if (statement.getUser()!=null) {
                                System.out.println(statement.getUser().toString());
                            }
                            break;
                        case PREPARE_SYNTAX_ERROR:
                            System.out.println("Syntax error");
                            break;
                        case PREPARE_UNRECOGNIZED_STATEMENT:
                            System.out.println("Unrecognized command '"+s+"'");
                            break;
                        default:
                            System.out.println("Unknown error");
                            break;
                    }
                    switch (excuteStatement(table,statement)){
                        case EXCUTE_SUCCESS:
                            System.out.println("SUCCESS");
                            break;
                        case EXCUTE_ERROR:
                            System.out.println("ERROR");
                            break;
                        case EXCUTE_TABLE_FULL:
                            System.out.println("TABLE_FULL");
                            break;
                        case EXCUTE_TABLE_LINE_LARGER:
                            System.out.println("TABLE_LINE_LARGER");
                            break;
                        default:
                            System.out.println("UNKNOWN");
                            break;
                    }
                }else{
                    System.out.println("Thanks for using~");
                    table.dbClose("user");
                    isExit=false;
                    br.close();
                    isr.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static ExcuteResult excuteStatement(Table t,Statement statement){
        if(TABLE_INSERT.equals(statement.getType())){
            t.insert(statement.getUser());
        }else if(TABLE_SELECT.equals(statement.getType())){
            t.select();
        }
        return EXCUTE_SUCCESS;
    }

    private static PrepareResult prepareStatement (String s, Statement statement) {
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
            statement.setUser(new User(Integer.parseInt(strings[1]),strings[2],strings[3]));

            return PREPARE_SUCCESS;
        }
        return PREPARE_UNRECOGNIZED_STATEMENT;
    }

    private static void printPrompt() {
        System.out.print("HengTianDB> ");
    }

    private static void printPromptFront() {
        System.out.println("This is HengTian database ,you can not input 'help' to get any help~");
    }
}
