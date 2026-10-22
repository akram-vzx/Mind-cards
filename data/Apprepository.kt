package com.mindcards.app.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AppRepository {
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    val cardSets = mutableListOf<CardSet>()
    private val progressMap = mutableMapOf<String, Progress>()
    var currentCardSetId: String? = null
        private set

    private const val KEY_CARD_SETS = "card_sets"
    private const val KEY_CURRENT = "current_id"
    private const val KEY_LAUNCHED = "has_launched"
    private const val P = "progress_"

    fun init(ctx: Context) {
        prefs = ctx.getSharedPreferences("mindcards", Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CARD_SETS, null)
        cardSets.clear()
        if (json != null) {
            val t = object : TypeToken<List<CardSet>>() {}.type
            val list: List<CardSet> = gson.fromJson(json, t) ?: emptyList()
            cardSets.addAll(list)
        }
        for (cs in cardSets) {
            progressMap[cs.id] = Progress(
                prefs.getInt("$P${cs.id}_a", 0),
                prefs.getInt("$P${cs.id}_c", 0)
            )
        }
        currentCardSetId = prefs.getString(KEY_CURRENT, null)

        val launched = prefs.getBoolean(KEY_LAUNCHED, false)
        if (!launched) {
            val b = BuiltInData.testCardSet()
            cardSets.add(b)
            progressMap[b.id] = Progress()
            currentCardSetId = b.id
            prefs.edit().putBoolean(KEY_LAUNCHED, true)
                .putString(KEY_CURRENT, b.id).apply()
            saveAll()
        } else if (cardSets.isNotEmpty() &&
            (currentCardSetId == null || cardSets.none { it.id == currentCardSetId })) {
            currentCardSetId = cardSets.first().id
            prefs.edit().putString(KEY_CURRENT, currentCardSetId).apply()
        }
    }

    fun saveAll() {
        prefs.edit().putString(KEY_CARD_SETS, gson.toJson(cardSets)).apply()
    }

    fun getCardSet(id: String) = cardSets.firstOrNull { it.id == id }
    fun getCurrentCardSet() = currentCardSetId?.let { getCardSet(it) }
    fun setCurrent(id: String) {
        currentCardSetId = id
        prefs.edit().putString(KEY_CURRENT, id).apply()
    }

    fun getProgress(id: String) = progressMap[id] ?: Progress()
    fun saveProgress(id: String, p: Progress) {
        progressMap[id] = p
        prefs.edit().putInt("$P${id}_a", p.answered)
            .putInt("$P${id}_c", p.correct).apply()
    }
    fun resetProgress(id: String) = saveProgress(id, Progress())

    fun addCardSet(cs: CardSet) {
        cardSets.add(cs)
        progressMap[cs.id] = Progress()
        saveAll()
    }
    fun removeCardSet(id: String) {
        cardSets.removeAll { it.id == id }
        progressMap.remove(id)
        prefs.edit().remove("$P${id}_a").remove("$P${id}_c").apply()
        saveAll()
        if (currentCardSetId == id) {
            currentCardSetId = cardSets.firstOrNull()?.id
            prefs.edit().putString(KEY_CURRENT, currentCardSetId).apply()
        }
    }

    fun globalAnswered() = progressMap.values.sumOf { it.answered }
    fun globalCorrect() = progressMap.values.sumOf { it.correct }
}
