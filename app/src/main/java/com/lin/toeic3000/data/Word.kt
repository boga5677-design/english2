package com.lin.toeic3000.data

data class Word(
    val id: Int,
    val english: String,
    val chinese: String
) {
    val level: String
        get() = when {
            id <= 1000 -> "初級"
            id <= 2000 -> "中級"
            else -> "進階"
        }

    val exampleEnglish: String
        get() = "I learned how to use the word \"$english\" today."

    val exampleChinese: String
        get() = "我今天學會了如何使用「$english」這個單字。"
}
