package com.mimo.babyassistantserver.entity;

/**
 * 当前支持的育儿记录类型。
 * 枚举值与数据库 record_type 以及 Flutter 请求中的 type 保持一致。
 */
public enum CareRecordType {
    FEEDING,
    DIAPER
}