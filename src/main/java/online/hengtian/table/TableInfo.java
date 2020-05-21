package online.hengtian.table;

import lombok.Data;

import java.util.Map;
@Data
public class TableInfo {
    /**
     * 表名
     */
    private String name;
    private Map<String,ColumnInfo> columns;

    private ColumnInfo pKey;


}
