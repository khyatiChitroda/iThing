package com.ithing.mobile.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String,
    val name: String,
    val oemId: String,
    val industry: String
)
