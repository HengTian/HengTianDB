package online.hengtian.table;


public enum MyDBTypeEnum {
    INT("Integer"),CHAR("String");

    private String javaType;
    MyDBTypeEnum(String javaType){
        this.javaType=javaType;
    }

    public String getJavaType() {
        return javaType;
    }
    public static String getType(String column){
        String s = column.toUpperCase();
        MyDBTypeEnum dbTypeEnum = MyDBTypeEnum.valueOf(s);
        return dbTypeEnum.getJavaType();
    }
}
