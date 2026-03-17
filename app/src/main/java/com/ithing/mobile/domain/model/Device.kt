package com.ithing.mobile.domain.model

data class Device(
    val id: String,
    val name: String,
    val customerId: String?,
    val oemId: String?,
    val industry: String?
)