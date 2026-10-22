package com.mindcards.app.data

object BuiltInData {
    fun testCardSet(): CardSet = CardSet(
        id = "builtin_test",
        name = "Test Card Set",
        version = "1.0",
        questions = listOf(
            Question("What is 2 + 2?", listOf("3", "4", "5", "6"), 1),
            Question("What color is the sky on a clear day?",
                listOf("Green", "Red", "Blue", "Yellow"), 2),
            Question("How many days are in a week?",
                listOf("5", "6", "7", "8"), 2),
            Question("What is the capital of France?",
                listOf("London", "Berlin", "Madrid", "Paris"), 3)
        )
    )
}
