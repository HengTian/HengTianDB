package online.hengtian.table;

import online.hengtian.utils.JavaBeanUtils;
import online.hengtian.utils.MyStringUtils;

import java.util.*;

public class Test {
    public static void main(String[] args) throws Exception {
        System.out.println(MyDBTypeEnum.getType("int"));

        System.out.println(MyStringUtils.getCamelCase("GOODS_Name",false));

        TableInfo tableInfo=new TableInfo();
        tableInfo.setName("user");
        tableInfo.setColumns(new LinkedHashMap<>());
        tableInfo.getColumns().put("1",new ColumnInfo("username","char"));
        tableInfo.getColumns().put("2",new ColumnInfo("age","int"));
        tableInfo.getColumns().put("3",new ColumnInfo("email_test","char"));

        String fileString = JavaBeanUtils.createFileString(tableInfo, new MyDBTypeConvertor());
        System.out.println(fileString);

//        JavaBeanUtils.createTableBean(tableInfo,new MyDBTypeConvertor());
        List<String> columns=new ArrayList<>();
        columns.add("HengTian");
        columns.add("12");
        columns.add("12312@qq.com");
        Object bean =JavaBeanUtils.injectProperties(columns, "User");
        Map<String,String> cols=new HashMap<>();
        cols.put("username","HengTian2");
        Object user = JavaBeanUtils.injectPropertiesByName(cols, "User");
    }
}
