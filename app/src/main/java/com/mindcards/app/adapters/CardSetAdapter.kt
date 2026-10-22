package com.mindcards.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mindcards.app.R
import com.mindcards.app.data.AppRepository
import com.mindcards.app.data.CardSet
import kotlin.math.roundToInt

class CardSetAdapter(
    private val onSelect: (CardSet) -> Unit,
    private val onReset: (CardSet) -> Unit
) : RecyclerView.Adapter<CardSetAdapter.VH>() {

    private var items: List<CardSet> = emptyList()

    fun submit(newItems: List<CardSet>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_set, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val cs = items[position]
        val p = AppRepository.getProgress(cs.id)
        val total = cs.questions.size
        val acc = if (p.answered > 0) (p.correct * 100.0 / p.answered).roundToInt() else 0

        h.name.text = cs.name
        h.version.text = cs.version
        h.progressText.text = h.itemView.context.getString(
            R.string.set_progress_format, p.answered, total
        )
        h.accuracyText.text = h.itemView.context.getString(
            R.string.set_accuracy_format, p.correct, p.answered - p.correct, acc
        )
        h.progressBar.max = total
        h.progressBar.progress = p.answered
        h.resetBtn.setOnClickListener { onReset(cs) }
        h.itemView.setOnClickListener { onSelect(cs) }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.setName)
        val version: TextView = v.findViewById(R.id.setVersion)
        val progressText: TextView = v.findViewById(R.id.setProgressText)
        val accuracyText: TextView = v.findViewById(R.id.setAccuracyText)
        val progressBar = v.findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.setProgressBar)
        val resetBtn = v.findViewById<TextView>(R.id.setResetBtn)
    }
}
