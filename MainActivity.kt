package com.mindcards.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.color.MaterialColors
import com.mindcards.app.adapters.TabsPagerAdapter
import com.mindcards.app.data.AppRepository
import com.mindcards.app.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun attachBaseContext(newBase: Context) {
        val lang = ThemeManager.getLang(newBase)
        val loc = Locale(lang)
        Locale.setDefault(loc)
        val cfg = Configuration(newBase.resources.configuration)
        cfg.setLocale(loc)
        cfg.setLayoutDirection(loc)
        super.attachBaseContext(newBase.createConfigurationContext(cfg))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = TabsPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.setCurrentItem(1, false)

        binding.navCards.setOnClickListener { binding.viewPager.setCurrentItem(0, true) }
        binding.navGame.setOnClickListener { binding.viewPager.setCurrentItem(1, true) }
        binding.navSettings.setOnClickListener { binding.viewPager.setCurrentItem(2, true) }

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateTitle(position)
                    updateNav(position)
                }
            }
        )
        updateTitle(1); updateNav(1)
    }

    private fun updateTitle(pos: Int) {
        binding.toolbarTitle.text = when (pos) {
            0 -> getString(R.string.tab_cards)
            1 -> AppRepository.getCurrentCardSet()?.name ?: getString(R.string.app_name)
            else -> getString(R.string.tab_settings)
        }
    }

    private fun updateNav(pos: Int) {
        val active = MaterialColors.getColor(binding.root,
            com.google.android.material.R.attr.colorPrimary)
        val inactive = MaterialColors.getColor(binding.root,
            com.google.android.material.R.attr.colorOnSurfaceVariant)
        binding.navCards.setTextColor(if (pos == 0) active else inactive)
        binding.navGame.setTextColor(if (pos == 1) active else inactive)
        binding.navSettings.setTextColor(if (pos == 2) active else inactive)
    }

    fun refreshTitle() = updateTitle(binding.viewPager.currentItem)
    fun goToCardsTab() = binding.viewPager.setCurrentItem(0, true)
    fun goToGameTab() {
        binding.viewPager.setCurrentItem(1, true)
        updateTitle(1)
    }
}
