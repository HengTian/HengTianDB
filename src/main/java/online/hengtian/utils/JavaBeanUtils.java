package online.hengtian.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import online.hengtian.memory.DbSystem;
import online.hengtian.table.ColumnInfo;
import online.hengtian.table.TableInfo;
import online.hengtian.table.TypeConvertor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JavaBeanUtils {
    public static void createTableBean(TableInfo tableInfo, TypeConvertor typeConvertor){
        String src=createFileString(tableInfo,typeConvertor);
        //将包名转换为文件名，然后和srcPath拼接
        String packagePath=DbSystem.TABLE_STORAGE_PACKAGE.replace(".","/");
        File f=new File(DbSystem.ROOT_PATH+packagePath);
        if (!f.exists()){
            f.mkdirs();
        }
        BufferedWriter bw=null;
        try {
            //将源码写入文件
            bw=new BufferedWriter(new FileWriter(f.getAbsoluteFile()+"/"+MyStringUtils.getCamelCase(tableInfo.getName(),true)+".java"));
            bw.write(src);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (bw!=null){
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static String createFileString(TableInfo tableInfo,TypeConvertor convertor){
        Map<String, ColumnInfo> columns=tableInfo.getColumns();
        StringBuilder src=new StringBuilder();
        //生成package语句
        src.append("package "+ DbSystem.TABLE_STORAGE_PACKAGE+";\n");
        //生成import语句
        src.append("import java.sql.*;\n");
        src.append("import lombok.Data;\n");
        src.append("import lombok.NoArgsConstructor;\n");
        src.append("import lombok.AllArgsConstructor;\n");
        src.append("import online.hengtian.table.TableBean;\n");
        src.append("import java.util.*;\n\n");
        src.append("@Data\n");
        src.append("@NoArgsConstructor\n");
        src.append("@AllArgsConstructor\n");
        src.append("@ToString\n");
        //生成类声明语句
        src.append("public class "+MyStringUtils.getCamelCase(tableInfo.getName(),true)+" extends TableBean implements Serializable {\n");
        //生成属性列表
        for (Map.Entry<String,ColumnInfo> row:tableInfo.getColumns().entrySet()){
            src.append("    private "+convertor.databaseType2JavaType(row.getValue().getDataType())+" "+ MyStringUtils.getCamelCase(row.getValue().getName(),false)+";\n");
        }
        //生成结束符
        src.append("}");
        return src.toString();
    }
    public static Object convert(Class<?> target,String s) {
        if (target == Object.class || target == String.class || s == null) {
            return s;
        }
        if (target == Character.class || target == char.class) {
            return s.charAt(0);
        }
        if (target == Byte.class || target == byte.class) {
            return Byte.parseByte(s);
        }
        if (target == Short.class || target == short.class) {
            return Short.parseShort(s);
        }
        if (target == Integer.class || target == int.class) {
            return Integer.parseInt(s);
        }
        if (target == Long.class || target == long.class) {
            return Long.parseLong(s);
        }
        if (target == Float.class || target == float.class) {
            return Float.parseFloat(s);
        }
        if (target == Double.class || target == double.class) {
            return Double.parseDouble(s);
        }
        if (target == Boolean.class || target == boolean.class) {
            return Boolean.parseBoolean(s);
        }
        throw new IllegalArgumentException("Don't know how to convert to " + target);
    }

    /**
     * 如果语法是insert User 1 2 3则默认是按建表时的顺序插入到字段
     * @param args
     * @param className
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public static Object injectProperties(List<String> args, String className) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // Load the class.
        Class<?> bean = Class.forName(DbSystem.TABLE_STORAGE_PACKAGE + "." + className);
        for (Constructor<?> ctor : bean.getConstructors()) {
            Class<?>[] paramTypes = ctor.getParameterTypes();
            if (args.size() == paramTypes.length) {
                // Convert the String arguments into the parameters' types.
                Object[] convertedArgs = new Object[args.size()];
                for (int i = 0; i < convertedArgs.length; i++) {
                    convertedArgs[i] = convert(paramTypes[i],args.get(i));
                }
                // Instantiate the object with the converted arguments.
                return ctor.newInstance(convertedArgs);
            }
        }
        throw new IllegalArgumentException("Don't know how to instantiate " + className);
    }
    /**
     * 如果语法时insert User(xxx,xxx,xxx) values(12,sdas,2313)则根据指定字段映射
     */
    public static Object injectPropertiesByName(Map<String,String> args, String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        Class<?> bean = Class.forName(DbSystem.TABLE_STORAGE_PACKAGE + "." + className);
        Object object = bean.newInstance();
        for(Map.Entry<String,String> arg:args.entrySet()){
            Field field = bean.getDeclaredField(arg.getKey());
            Class<?> type = field.getType();
            field.setAccessible(true);
            field.set(object,convert(type,arg.getValue()));
        }
        return object;
    }
}
