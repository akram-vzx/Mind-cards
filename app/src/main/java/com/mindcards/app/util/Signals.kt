package com.mindcards.app.util

object GameReloadSignal {
    private val listeners = mutableListOf<() -> Unit>()
    fun subscribe(l: () -> Unit) { listeners.add(l) }
    fun unsubscribe(l: () -> Unit) { listeners.remove(l) }
    fun fire() { listeners.toList().forEach { it.invoke() } }
}

object CardReloadSignal {
    private val listeners = mutableListOf<() -> Unit>()
    fun subscribe(l: () -> Unit) { listeners.add(l) }
    fun unsubscribe(l: () -> Unit) { listeners.remove(l) }
    fun fire() { listeners.toList().forEach { it.invoke() } }
}
