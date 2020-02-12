package com.avon.spott.Utils

import java.util.regex.Matcher
import java.util.regex.Pattern

class Validator {
    companion object {
        fun validEmail(email: String): Boolean {
            val reg =
                Regex("(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")

            return email.matches(reg)
        }

        fun validPassword(string: String): Boolean {
            val reg = Regex("((?=.*\\d)(?=.*[a-zA-Z])(?=.*\\W).{6,20})")

            return string.matches(reg)
        }

        fun validNumber(number:String):Boolean {
            return number.length > 0
        }

        fun validHashtag(string: String): Matcher {
            val pattern = Pattern.compile("\\#([0-9a-zA-Z가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]*)")
            return pattern.matcher(string)
        }
    }
}