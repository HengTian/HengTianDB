package online.hengtian.myperl;

import lombok.Data;
import lombok.ToString;
import online.hengtian.table.TableBean;
import online.hengtian.table.TableInfo;

import java.util.List;
import java.util.Map;

@Data
@ToString
public class Statement<T extends TableBean> {
    private String type;
    private T bean;
    private TableInfo tableInfo;
    private List<String> params;
    private Map<String,String> map;
    public Statement() {
        tableInfo=new TableInfo();
    }
}
