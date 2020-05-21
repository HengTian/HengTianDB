package online.hengtian.table;

public class MyDBTypeConvertor implements TypeConvertor {
    public String databaseType2JavaType(String column) {
        return MyDBTypeEnum.getType(column);
    }
}
