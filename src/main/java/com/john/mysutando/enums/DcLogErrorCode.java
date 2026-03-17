package com.john.mysutando.enums;

import lombok.Getter;

@Getter
public enum DcLogErrorCode {
    API_CONNECTION_FAILED("API 網路連線異常"),
    INVALID_FORMAT("資料格式錯誤 (例如 Snowflake ID 異常)"),
    SYNC_MAX_RETRIES("歷史同步已達最大重試次數"),
    UNKNOWN_ERROR("未知的系統錯誤");

    private final String description;

    DcLogErrorCode(String description) {
        this.description = description;
    }
}
