package online.hengtian.table;


public enum MyDBTypeEnum {
    INT("Integer"),STRING("String"),FLOAT("Float"),DOUBLE("Double"),LONG("Long"),DATE("Date"),CHAR("Character"),BOOL("Boolean");

    private String javaType;
    MyDBTypeEnum(String javaType){
        this.javaType=javaType;
    }

    public String getJavaType() {
        return javaType;
    }
    public static String getType(String column) throws Exception{
        String s = column.toUpperCase();
        MyDBTypeEnum dbTypeEnum = MyDBTypeEnum.valueOf(s);
        return dbTypeEnum.getJavaType();
    }
}
