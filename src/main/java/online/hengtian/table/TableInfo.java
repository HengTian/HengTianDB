package online.hengtian.table;

import lombok.Data;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;
@Data
@ToString
public class TableInfo {
    /**
     * 表名
     */
    private String name;
    private LinkedHashMap<String,ColumnInfo> columns;

    private ColumnInfo pKey;


}
