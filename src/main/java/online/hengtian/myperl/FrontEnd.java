package online.hengtian.myperl;

import online.hengtian.memory.Table;
import online.hengtian.memory.Tabler;
import online.hengtian.table.MyDBTypeConvertor;
import online.hengtian.utils.JavaBeanUtils;
import online.hengtian.utils.MyFileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import static online.hengtian.memory.DbSystem.*;
import static online.hengtian.myperl.ExcuteResult.*;

//后面可以换成JLine框架实现自动补全等命令行常用功能

/**
 * @author 陆子恒
 * @date 2020.4.14
 */
public class FrontEnd {
    public final static File redoLog=new File("redo.log");
    public final static Tabler tabler=new Tabler();

    /**
     * 读取输入直到;结束
     * @param br
     * @return
     * @throws IOException
     */
    public static String readCommand(BufferedReader br) throws IOException {
        StringBuffer sb=new StringBuffer();
        String s;
        printPrompt();
        do{
            s=br.readLine();
            sb.append(" "+s);
            printPromptContinue();
        }while(!s.endsWith(";"));
        return sb.toString();
    }
    public static void print(String s){
        System.out.println(s);
    }
    public static void main(String[] args){
        printPromptFront();//打印提示内容
        boolean isExit=true;
//        Table table=new Table().dbOpen("user");
        while(isExit){
            Statement statement=new Statement();
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(isr);
            try{
                String s=readCommand(br);
                System.out.println(s);
                if (!s.contains("exit")){
                    PrepareResult prepareResult = prepareStatement(s, statement);
                    switch (prepareResult){
                        case PREPARE_SUCCESS:
                            MyFileUtils.writeLine(s);
                            break;
                        default:
                            print(prepareResult.getMessage());
                            continue;
                    }
                    ExcuteResult excuteResult = excuteStatement(tabler, statement);
                    print(excuteResult.getMessage());
                }else{
                    print("Thanks for using~");
                    for(Map.Entry<String,Table> tableEntry: DB_TABLES.entrySet()){
                        tabler.dbClose(tableEntry.getKey());
                    }
                    isExit=false;
                }
            } catch (IOException e) {
                isExit=false;
                e.printStackTrace();
            }
        }
    }

    private static ExcuteResult excuteStatement(Tabler t,Statement statement){
        switch (statement.getType()){
            case TABLE_INSERT:
                return t.insertOrUpdate(statement.getTableInfo().getName(),statement.getBean());
            case TABLE_SELECT:
                return t.select(statement.getTableInfo().getName(),statement.getParams());
            case TABLE_UPDATE:
                return t.insertOrUpdate(statement.getTableInfo().getName(),statement.getBean());
            case TABLE_DELETE:
                return t.delete(statement.getTableInfo().getName(),statement.getParams());
            case TABLE_CREATE:
                try {
                    JavaBeanUtils.createTableBean(statement.getTableInfo(),new MyDBTypeConvertor());
                    tabler.dbOpen(statement.getTableInfo().getName(),true);
                } catch (Exception e) {
                    e.printStackTrace();
                    return EXCUTE_ERROR;
                }
                break;
            case TABLE_DROP:
                return t.drop(statement.getTableInfo().getName());
            default:
                return EXCUTE_SUCCESS;
        }
//        if(TABLE_INSERT.equals(statement.getType())){
//            t.setModify(false);
//            if(!t.insert(statement.getBean())){
//                return EXCUTE_ERROR;
//            }
//        }else if(TABLE_SELECT.equals(statement.getType())){
//            t.select();
//        }else if(TABLE_UPDATE.equals(statement.getType())){
//            t.setModify(true);
//            t.insert(statement.getBean());
//        }else if(TABLE_DELETE.equals(statement.getType())){
//            if(!t.delete(statement.getBean())){
//                return EXCUTE_ERROR;
//            }
//        }
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
    private static void printPromptContinue() {
        System.out.print("        --> ");
    }

    private static void printPromptFront() {
        System.out.println("This is HengTian database ,you can not input 'help' to get any help~");
    }
}
