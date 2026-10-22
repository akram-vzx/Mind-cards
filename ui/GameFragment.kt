package com.mindcards.app.ui

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.mindcards.app.MainActivity
import com.mindcards.app.R
import com.mindcards.app.data.AppRepository
import com.mindcards.app.data.CardSet
import com.mindcards.app.data.Progress
import com.mindcards.app.data.Question
import com.mindcards.app.databinding.FragmentGameBinding
import com.mindcards.app.util.CardReloadSignal
import com.mindcards.app.util.GameReloadSignal
import kotlin.math.roundToInt

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private var currentSet: CardSet? = null
    private var questions: List<Question> = emptyList()
    private var currentIndex = 0
    private var answered = false
    private var flashAnimator: ValueAnimator? = null

    private val reloadListener: () -> Unit = { loadCardSet() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.skipButton.setOnClickListener { onSkip() }
        binding.completedRestart.setOnClickListener { confirmRestart() }
        binding.completedOtherSets.setOnClickListener {
            (activity as? MainActivity)?.goToCardsTab()
        }
        GameReloadSignal.subscribe(reloadListener)
        loadCardSet()
    }

    override fun onDestroyView() {
        GameReloadSignal.unsubscribe(reloadListener)
        flashAnimator?.cancel()
        _binding = null
        super.onDestroyView()
    }

    private fun loadCardSet() {
        val cs = AppRepository.getCurrentCardSet()
        currentSet = cs
        if (cs == null) {
            binding.content.visibility = View.GONE
            binding.completedState.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
            return
        }
        questions = cs.questions
        val p = AppRepository.getProgress(cs.id)
        currentIndex = p.answered
        if (currentIndex >= questions.size) showCompleted() else showQuestion()
    }

    private fun showQuestion() {
        binding.emptyState.visibility = View.GONE
        binding.completedState.visibility = View.GONE
        binding.content.visibility = View.VISIBLE
        answered = false
        val q = questions[currentIndex]
        binding.questionText.text = q.question

        val buttons = listOf(binding.optionA, binding.optionB, binding.optionC, binding.optionD)
        buttons.forEachIndexed { i, btn ->
            btn.text = q.options.getOrNull(i) ?: ""
            resetOption(btn)
            btn.isEnabled = true
            btn.setOnClickListener { onAnswer(i) }
        }
        binding.skipButton.visibility = View.GONE
        binding.progressText.text = "${currentIndex + 1} / ${questions.size}"
        binding.progressBar.max = questions.size
        binding.progressBar.progress = currentIndex
    }

    private fun resetOption(btn: MaterialButton) {
        val bg = MaterialColors.getColor(btn, com.google.android.material.R.attr.colorSecondaryContainer)
        val fg = MaterialColors.getColor(btn, com.google.android.material.R.attr.colorOnSecondaryContainer)
        btn.backgroundTintList = ColorStateList.valueOf(bg)
        btn.setTextColor(fg)
    }

    private fun paintCorrect(btn: MaterialButton) {
        btn.backgroundTintList = ColorStateList.valueOf(0xFF43A047.toInt())
        btn.setTextColor(Color.WHITE)
    }

    private fun paintWrong(btn: MaterialButton) {
        btn.backgroundTintList = ColorStateList.valueOf(0xFFE53935.toInt())
        btn.setTextColor(Color.WHITE)
    }

    private fun onAnswer(index: Int) {
        if (answered) return
        val cs = currentSet ?: return
        val q = questions[currentIndex]
        answered = true

        val buttons = listOf(binding.optionA, binding.optionB, binding.optionC, binding.optionD)
        buttons.forEach { it.isEnabled = false }

        val p = AppRepository.getProgress(cs.id)
        if (index == q.correctIndex) {
            paintCorrect(buttons[index])
            AppRepository.saveProgress(cs.id, Progress(p.answered + 1, p.correct + 1))
        } else {
            flashRed()
            paintWrong(buttons[index])
            paintCorrect(buttons[q.correctIndex])
            AppRepository.saveProgress(cs.id, Progress(p.answered + 1, p.correct))
        }
        CardReloadSignal.fire()
        binding.skipButton.visibility = View.VISIBLE
    }

    private fun flashRed() {
        flashAnimator?.cancel()
        val ov = binding.flashOverlay
        ov.visibility = View.VISIBLE
        ov.alpha = 0f
        flashAnimator = ValueAnimator.ofFloat(0f, 0.35f, 0f).apply {
            duration = 500
            addUpdateListener { ov.alpha = it.animatedValue as Float }
            start()
        }
        ov.postDelayed({ if (_binding != null) ov.visibility = View.GONE }, 500)
    }

    private fun onSkip() {
        currentIndex++
        if (currentIndex >= questions.size) showCompleted() else showQuestion()
    }

    private fun showCompleted() {
        binding.content.visibility = View.GONE
        binding.emptyState.visibility = View.GONE
        binding.completedState.visibility = View.VISIBLE
        val cs = currentSet ?: return
        val p = AppRepository.getProgress(cs.id)
        val acc = if (p.answered > 0) (p.correct * 100.0 / p.answered).roundToInt() else 0
        binding.completedMessage.text =
            getString(R.string.completed_message, p.correct, p.answered, acc)
    }

    private fun confirmRestart() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.start_over_title)
            .setMessage(R.string.start_over_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                currentSet?.let { AppRepository.resetProgress(it.id) }
                CardReloadSignal.fire()
                loadCardSet()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}
