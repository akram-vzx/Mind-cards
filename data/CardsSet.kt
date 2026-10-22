package com.mindcards.app.data

data class CardSet(
    val id: String,
    val name: String,
    val version: String,
    val questions: List<Question>
)

data class Progress(
    val answered: Int = 0,
    val correct: Int = 0
)
