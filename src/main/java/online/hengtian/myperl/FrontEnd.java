package online.hengtian.myperl;

import online.hengtian.memory.DbSystem;
import online.hengtian.memory.Page;
import online.hengtian.memory.Table;
import online.hengtian.mytree.BPlusTree;
import online.hengtian.table.TableBean;
import online.hengtian.table.User;
import online.hengtian.utils.MyFileUtils;
import online.hengtian.utils.SerializeUtils;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
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
                StringBuffer sb=new StringBuffer();
                while(!s.endsWith(";")){
                    sb.append(s);
                    continue;
                }
                s=sb.toString();
                System.out.println(s);
                if (!"exit;".equals(s)){
                    switch (prepareStatement(s,statement)){
                        case PREPARE_SUCCESS:
                            System.out.println(statement.getType()+" success");
                            if (statement.getBean()!=null) {
                                System.out.println(statement.getBean().toString());
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
            t.setModify(false);
            if(!t.insert(statement.getBean())){
                return EXCUTE_ERROR;
            }
        }else if(TABLE_SELECT.equals(statement.getType())){
            t.select();
        }else if(TABLE_UPDATE.equals(statement.getType())){
            t.setModify(true);
            t.insert(statement.getBean());
        }else if(TABLE_DELETE.equals(statement.getType())){
            if(!t.delete(statement.getBean())){
                return EXCUTE_ERROR;
            }
        }
        return EXCUTE_SUCCESS;
    }

    /**
     * 语法检查
     * @param s
     * @param statement
     * @return
     */
    private static PrepareResult prepareStatement (String s, Statement statement){
        return ParseCommand.prepareStatement(s,statement);
    }

    private static void printPrompt() {
        System.out.print("HengTianDB> ");
    }

    private static void printPromptFront() {
        System.out.println("This is HengTian database ,you can not input 'help' to get any help~");
    }
}
