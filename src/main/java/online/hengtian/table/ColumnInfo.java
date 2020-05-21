package online.hengtian.table;

import lombok.Data;

@Data
public class ColumnInfo {
    private String name;
    private String dataType;

    public ColumnInfo(String name, String dataType) {
        this.name = name;
        this.dataType = dataType;
    }
}
