package com.ithing.mobile.core.security

import java.security.MessageDigest

object HashUtil {
    fun sha1(input: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-1")
            .digest(input.toByteArray())

        return bytes.joinToString("") { "%02x".format(it) }
    }
}
