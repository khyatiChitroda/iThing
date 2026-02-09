package com.ithing.mobile.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val profileImageUrl: String?
)