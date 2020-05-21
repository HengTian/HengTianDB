package online.hengtian.myperl;

public enum PrepareResult {
    PREPARE_SUCCESS("解析成功"),
    PREPARE_UNRECOGNIZED_STATEMENT("未识别的命令"),
    PREPARE_SYNTAX_ERROR("解析错误");

    private String message;

    PrepareResult(String message) {

        this.message = message;
    }
}
