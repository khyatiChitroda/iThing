package com.ithing.mobile.data.mapper

import com.ithing.mobile.data.local.entity.UserEntity
import com.ithing.mobile.data.remote.dto.UserDto
import com.ithing.mobile.domain.model.User

fun UserDto.toDomain(): User =
    User(
        id = id,
        name = name,
        email = email,
        profileImageUrl = profileImageUrl
    )

fun UserEntity.toDomain(): User =
    User(
        id = id,
        name = name,
        email = email,
        profileImageUrl = profileImageUrl
    )

fun User.toEntity(): UserEntity =
    UserEntity(
        id = id,
        name = name,
        email = email,
        profileImageUrl = profileImageUrl
    )