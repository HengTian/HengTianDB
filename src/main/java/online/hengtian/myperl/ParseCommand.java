package online.hengtian.myperl;

import online.hengtian.memory.Table;
import online.hengtian.table.ColumnInfo;
import online.hengtian.table.MyDBTypeConvertor;
import online.hengtian.table.TableBean;
import online.hengtian.utils.JavaBeanUtils;
import online.hengtian.utils.MyFileUtils;
import online.hengtian.utils.MyStringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.sun.org.apache.xalan.internal.lib.ExsltStrings.split;
import static online.hengtian.memory.DbSystem.*;
import static online.hengtian.memory.DbSystem.TABLE_DELETE;
import static online.hengtian.myperl.PrepareResult.*;

public class ParseCommand {
    /**
     * 语法解析和词法解析
     * @param s
     * @param statement
     * @return
     */
    public static PrepareResult prepareStatement (String s, Statement statement){
        //去除前面的空格
        s=preParse(s).toLowerCase();
        if(s==null) return PREPARE_SYNTAX_ERROR;
        if (s.contains(TABLE_SELECT)){
            statement.setType(TABLE_SELECT);
            if(!parseInsert(s,statement)) return PREPARE_SELECT_ERROR;
            return PREPARE_SUCCESS;
        }else if (s.contains(TABLE_INSERT)){
            boolean insert = parseInsert(s, statement);
            if(insert==false) return PREPARE_INSERT_ERROR;
            statement.setType(TABLE_INSERT);
            try {
                statement.setBean(JavaBeanUtils.injectProperties((List<String>) statement.getParams(),statement.getTableInfo().getName()));
            } catch (ClassNotFoundException e) {
                return PREPARE_TABLE_NOT_EXISTS;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (Exception e){
                return PREPARE_INSERT_ERROR;
            }
            return PREPARE_SUCCESS;
        }else if(s.contains(TABLE_UPDATE)){
            statement.setType(TABLE_UPDATE);
            if(!parseInsert(s,statement)) return PREPARE_UPDATE_ERROR;
            try {
                statement.setBean(JavaBeanUtils.injectProperties((List<String>) statement.getParams().subList(1,statement.getParams().size()),statement.getTableInfo().getName()));
                statement.getBean().setId(Long.parseLong((String)statement.getParams().get(0)));
            } catch (ClassNotFoundException e) {
                return PREPARE_TABLE_NOT_EXISTS;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (Exception e){
                return PREPARE_UPDATE_ERROR;
            }
            return PREPARE_SUCCESS;
        }else if(s.contains(TABLE_DELETE)){
            statement.setType(TABLE_DELETE);
            if(!parseInsert(s,statement)) return PREPARE_DELETE_ERROR;
            return PREPARE_SUCCESS;
        }else if(s.contains(TABLE_CREATE)){
            boolean create = parseCreate(s, statement);
            if(create==false) return PREPARE_CREATE_ERROR;
            if(DB_TABLES.get(statement.getTableInfo().getName())!=null){
                return PREPARE_TABLE_EXISTS;
            }
            statement.setType(TABLE_CREATE);
            return checkCreateType(statement.getTableInfo().getColumns());
        }else if(s.contains(TABLE_DROP)){
            statement.setType(TABLE_DROP);
            if(!parseInsert(s,statement)) return PREPARE_DROP_ERROR;
            return PREPARE_SUCCESS;
        }
        return PREPARE_UNRECOGNIZED_STATEMENT;
    }

    public static PrepareResult checkCreateType(LinkedHashMap<String,ColumnInfo> columns){
        for(Map.Entry<String,ColumnInfo> column:columns.entrySet()){
            String s = null;
            try {
                s = new MyDBTypeConvertor().databaseType2JavaType(column.getValue().getDataType());
            } catch (Exception e) {
                return PREPARE_TYPE_ERROR;
            }
        }
        return PREPARE_SUCCESS;
    }
    public static String preParse(String s){
        StringBuffer sb=new StringBuffer();
        boolean flag=false;
        for(int i=0;i<s.length();i++){
            if(s.charAt(i)!=' ') flag=true;
            if(flag){
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }

    public static int getTableName(String s,Statement statement){
        int tableIndex = s.indexOf("table");
        if(tableIndex==-1&&tableIndex+5==s.length()-1) return -1;
        tableIndex+=5;
        return tableIndex;
    }
    /**
     * 解析如下格式的
     * create table xxx(xx int);
     * @param s
     * @param statement
     * @return
     */
    public static boolean parseCreate(String s,Statement statement){
        int tableIndex = getTableName(s, statement);
        if(tableIndex==-1) return false;
        String[] strings = getTableParam(s, tableIndex);
        statement.getTableInfo().setName(MyStringUtils.getCamelCase(strings[0],true));
        if(strings==null||(strings.length-1)%2!=0) return false;
        LinkedHashMap<String,ColumnInfo> tableColumn=new LinkedHashMap<>();
        for(int i=1;i<strings.length;i+=2){
            ColumnInfo column = new ColumnInfo(strings[i], strings[i + 1]);
            tableColumn.put(i+"",column);
        }
        statement.getTableInfo().setColumns(tableColumn);
        return true;
    }
    public static String[] getTableParam(String s,int tableIndex){
        String[] strings = s.substring(tableIndex).split("\\s+|,");
        strings=clearStrings(strings);
        return strings;
    }
    public static boolean parseInsert(String s,Statement statement){
        int tableIndex = getTableName(s, statement);
        if(tableIndex==-1) return false;
        String[] strings = getTableParam(s, tableIndex);
        if(strings==null) return false;
        statement.getTableInfo().setName(MyStringUtils.getCamelCase(strings[0],true));
        statement.setParams(new ArrayList<>());
        for(int i=1;i<strings.length;i++){
            statement.getParams().add(strings[i]);
        }
        return true;
    }

    public static String clear(String s){
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<s.length();i++){
            if(s.charAt(i)!='('&&s.charAt(i)!=')'&&s.charAt(i)!=';'){
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }
    public static String[] clearStrings(String[] s){
        List<String> result=new ArrayList<>();
        for(int i=0;i<s.length;i++){
            if(clear(s[i]).length()>0) result.add(clear(s[i]));
        }
        return result.toArray(new String[result.size()]);
    }
    public static void main(String[] args){
        System.out.println(preParse("   create  "));
        System.out.println("create table xxx(".indexOf("table"));
        Statement<TableBean> tableBeanStatement = new Statement<>();
        parseCreate("create table xxx(userName int,email char);",tableBeanStatement);
        System.out.println(tableBeanStatement.getTableInfo().getName());
    }
}
