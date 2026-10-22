package com.mindcards.app.data

object CardSetParser {
    /**
     * Format:
     * NAME: My Set
     * VERSION: 1.0
     * ===
     * Q: Question text?
     * A) option 1
     * B) option 2
     * C) option 3
     * D) option 4
     * ANSWER: B
     * ===
     */
    fun parse(text: String, fallbackName: String, id: String): CardSet? {
        try {
            val lines = text.lines()
            var name = fallbackName
            var version = "1.0"
            val questions = mutableListOf<Question>()

            var i = 0
            while (i < lines.size) {
                val line = lines[i].trim()
                when {
                    line.startsWith("NAME:", true) -> name = line.substringAfter(":").trim()
                    line.startsWith("VERSION:", true) -> version = line.substringAfter(":").trim()
                    line.startsWith("Q:", true) -> break
                }
                i++
            }

            var curQ: String? = null
            var opts = mutableListOf<String>()
            var correct = -1

            while (i < lines.size) {
                val line = lines[i].trim()
                when {
                    line.startsWith("Q:", true) -> {
                        if (curQ != null && opts.size >= 2 && correct >= 0) {
                            questions.add(Question(curQ!!, opts, correct))
                        }
                        curQ = line.substringAfter(":").trim()
                        opts = mutableListOf()
                        correct = -1
                    }
                    line.matches(Regex("^[A-Da-d]\\)\\s*.+")) -> {
                        opts.add(line.substringAfter(")").trim())
                    }
                    line.startsWith("ANSWER:", true) -> {
                        correct = when (line.substringAfter(":").trim().uppercase().firstOrNull()) {
                            'A' -> 0; 'B' -> 1; 'C' -> 2; 'D' -> 3; else -> -1
                        }
                    }
                }
                i++
            }
            if (curQ != null && opts.size >= 2 && correct >= 0) {
                questions.add(Question(curQ!!, opts, correct))
            }
            if (questions.isEmpty()) return null
            return CardSet(id, name, version, questions)
        } catch (e: Exception) { return null }
    }
}
