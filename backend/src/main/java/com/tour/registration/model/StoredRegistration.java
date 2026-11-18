package com.tour.registration.model;

/**
 * 存在試算表裡的一列。
 * tokenHash 是「修改憑證」的 SHA-256 雜湊；憑證本身只交給報名人，不存原文。
 */
public record StoredRegistration(
        String id,
        String tokenHash,
        String submittedAt,
        String updatedAt,
        Registration data
) {
}
