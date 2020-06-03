package online.hengtian.myperl;

public enum ExcuteResult {
    EXCUTE_SUCCESS("执行成功"),
    EXCUTE_TABLE_NOT_EXIST("该表不存在!"),
    EXCUTE_TABLE_NULL("该表无可删除记录!"),
    EXCUTE_PARAM_ERROR("输入参数不正确!"),
    EXCUTE_TYPE_ERROR("输入字段不正确!"),
    EXCUTE_ERROR("执行失败");
    private String message;

    ExcuteResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
