package com.eyther.lumbridge.features.expenses.domain.model

data class SettlementDomain(
    val id: String,
    val from: String,
    val to: String,
    val amountMinor: Long,
    val occurredOn: String,
    val accountId: String = "",
    val createdBy: String = "",
    val version: Long = 0,
) {
    fun canManage(userId: String, isOwner: Boolean): Boolean =
        isOwner || userId == createdBy || userId == from || userId == to
}
