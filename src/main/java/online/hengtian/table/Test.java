package online.hengtian.table;

import online.hengtian.utils.JavaBeanUtils;
import online.hengtian.utils.MyStringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws Exception {
        System.out.println(MyDBTypeEnum.getType("int"));

        System.out.println(MyStringUtils.getCamelCase("GOODS_Name",false));

        TableInfo tableInfo=new TableInfo();
        tableInfo.setName("user");
        tableInfo.setColumns(new HashMap<>());
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
        System.out.println(((online.hengtian.tablebean.User) bean));
        Map<String,String> cols=new HashMap<>();
        cols.put("username","HengTian2");
        Object user = JavaBeanUtils.injectPropertiesByName(cols, "User");
        System.out.println(((online.hengtian.tablebean.User) user));
    }
}
