package online.hengtian.myperl;

import lombok.Data;
import online.hengtian.table.TableBean;
@Data
public class Statement<T extends TableBean> {
    private String type;
    private T bean;
    private String tableName;
}
