package online.hengtian.table;

public interface TypeConvertor {
    /**
     * 将数据库类型转化成Java类型
     * @param columnType
     * @return
     */
    String databaseType2JavaType(String columnType) throws Exception;
}
