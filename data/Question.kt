package com.mindcards.app.data

data class Question(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)
