package com.mindcards.app.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mindcards.app.R
import com.mindcards.app.ThemeManager
import com.mindcards.app.data.AppRepository
import com.mindcards.app.databinding.FragmentSettingsBinding
import kotlin.math.roundToInt

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshStats()
        setupThemePicker()
        setupDarkMode()
        setupLanguage()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun refreshStats() {
        val answered = AppRepository.globalAnswered()
        val correct = AppRepository.globalCorrect()
        val wrong = answered - correct
        val acc = if (answered > 0) (correct * 100.0 / answered).roundToInt() else 0

        binding.statAnswered.text = answered.toString()
        binding.statCorrect.text = correct.toString()
        binding.statWrong.text = wrong.toString()
        binding.statAccuracy.text = "$acc%"

        binding.perSetContainer.removeAllViews()
        for (cs in AppRepository.cardSets) {
            val p = AppRepository.getProgress(cs.id)
            val a = if (p.answered > 0) (p.correct * 100.0 / p.answered).roundToInt() else 0
            val row = layoutInflater.inflate(R.layout.item_stat, binding.perSetContainer, false)
            row.findViewById<TextView>(R.id.statSetName).text = cs.name
            row.findViewById<TextView>(R.id.statSetValue).text =
                getString(R.string.set_stat_format, p.answered, cs.questions.size, a)
            binding.perSetContainer.addView(row)
        }
    }

    private fun setupThemePicker() {
        val items = listOf(
            "blue" to 0xFF4A5C92.toInt(),
            "purple" to 0xFF6750A4.toInt(),
            "green" to 0xFF386A20.toInt(),
            "teal" to 0xFF006A6A.toInt(),
            "orange" to 0xFF8B5000.toInt(),
            "pink" to 0xFF984061.toInt()
        )
        val current = ThemeManager.getThemeKey(requireContext())
        binding.themeContainer.removeAllViews()
        val d = resources.displayMetrics.density
        val size = (36 * d).toInt()
        val margin = (6 * d).toInt()
        for ((key, color) in items) {
            val circle = View(requireContext())
            val lp = LinearLayout.LayoutParams(size, size)
            lp.marginEnd = margin
            circle.layoutParams = lp
            val gd = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
                if (key == current) setStroke((3 * d).toInt(), 0xFFFFFFFF.toInt())
            }
            circle.background = gd
            circle.setOnClickListener {
                ThemeManager.setTheme(requireContext(), key)
                requireActivity().recreate()
            }
            binding.themeContainer.addView(circle)
        }
    }

    private fun setupDarkMode() {
        binding.darkModeSwitch.isChecked = ThemeManager.isDark(requireContext())
        binding.darkModeSwitch.setOnCheckedChangeListener { _, checked ->
            ThemeManager.setDark(requireContext(), checked)
        }
    }

    private fun setupLanguage() {
        val lang = ThemeManager.getLang(requireContext())
        val rb: RadioButton = if (lang == "en") binding.langEn else binding.langAr
        rb.isChecked = true
        binding.langGroup.setOnCheckedChangeListener { _, id ->
            val newLang = if (id == R.id.langEn) "en" else "ar"
            if (newLang != ThemeManager.getLang(requireContext())) {
                ThemeManager.setLang(requireContext(), newLang)
                requireActivity().recreate()
            }
        }
    }
}
