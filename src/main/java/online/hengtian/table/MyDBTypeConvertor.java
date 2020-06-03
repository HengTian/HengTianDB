package online.hengtian.table;

public class MyDBTypeConvertor implements TypeConvertor {
    public String databaseType2JavaType(String column) throws Exception{
        return MyDBTypeEnum.getType(column);
    }
}
