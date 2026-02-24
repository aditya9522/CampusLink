package com.example.eventapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.eventapp.R
import com.example.eventapp.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var themeManager: ThemeManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        themeManager = ThemeManager(requireContext())

        binding.btnThemeSelector.setOnClickListener {
            showThemeSelector()
        }

        // Observe theme changes and update UI
        lifecycleScope.launch {
            themeManager.getThemeMode().collect { mode ->
                val themeText = when (mode) {
                    AppCompatDelegate.MODE_NIGHT_NO -> getString(R.string.settings_theme_light)
                    AppCompatDelegate.MODE_NIGHT_YES -> getString(R.string.settings_theme_dark)
                    else -> getString(R.string.settings_theme_system)
                }
                binding.tvCurrentTheme.text = themeText
                AppCompatDelegate.setDefaultNightMode(mode)
            }
        }
    }

    private fun showThemeSelector() {
        val themes = arrayOf(
            getString(R.string.settings_theme_system),
            getString(R.string.settings_theme_light),
            getString(R.string.settings_theme_dark)
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_theme)
            .setItems(themes) { _, which ->
                val mode = when (which) {
                    0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    1 -> AppCompatDelegate.MODE_NIGHT_NO
                    2 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                // Save the theme preference
                lifecycleScope.launch {
                    themeManager.setThemeMode(mode)
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
