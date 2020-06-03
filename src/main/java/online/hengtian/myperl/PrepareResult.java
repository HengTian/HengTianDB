package online.hengtian.myperl;

public enum PrepareResult {
    PREPARE_SUCCESS("解析成功"),
    PREPARE_UNRECOGNIZED_STATEMENT("未识别的命令"),
    PREPARE_SYNTAX_ERROR("解析错误"),
    PREPARE_CREATE_ERROR("建表解析错误"),
    PREPARE_TABLE_EXISTS("表已存在"),
    PREPARE_TABLE_NOT_EXISTS("该表不存在!!"),
    PREPARE_SELECT_ERROR("查询解析错误"),
    PREPARE_UPDATE_ERROR("更新解析错误"),
    PREPARE_DELETE_ERROR("删除解析错误"),
    PREPARE_DROP_ERROR("删除表解析错误"),
    PREPARE_TYPE_ERROR("创建表字段类型错误"),
    PREPARE_INSERT_ERROR("插入解析错误");

    private String message;

    PrepareResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
