package org.url_shortner.service

import org.springframework.stereotype.Component

@Component
class Base62Encoder {


    private val alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val base = alphabet.length.toLong()

    fun encode(id: Long): String {
        require(id > 0) { "ID must be positive" }
        var num = id
        val sb = StringBuilder()
        while (num > 0) {
            sb.append(alphabet[(num % base).toInt()])
            num /= base
        }
        return sb.reverse().toString().padStart(6, '0')
    }

    fun decode(code: String): Long {
        var result = 0L
        for (ch in code) {
            result = result * base + alphabet.indexOf(ch)
        }
        return result
    }
}