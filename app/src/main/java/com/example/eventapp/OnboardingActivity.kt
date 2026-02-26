package com.example.eventapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge

import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.eventapp.databinding.ActivityOnboardingBinding
import kotlinx.coroutines.launch

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private var currentStep = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDropdowns()
        setupListeners()
    }

    private fun setupDropdowns() {
        val countries = resources.getStringArray(R.array.onboarding_countries)
        binding.dropdownCountry.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, countries)
        )

        val repository = (application as EventApplication).repository
        lifecycleScope.launch {
            try {
                val colleges = repository.getColleges()
                val collegeNames = colleges.map { it.name }
                binding.dropdownCollege.setAdapter(
                    ArrayAdapter(this@OnboardingActivity, android.R.layout.simple_list_item_1, collegeNames)
                )
            } catch (e: Exception) {
                Toast.makeText(this@OnboardingActivity, "Failed to load colleges", Toast.LENGTH_SHORT).show()
                // Fallback to resources if API fails
                val colleges = resources.getStringArray(R.array.onboarding_colleges)
                binding.dropdownCollege.setAdapter(
                    ArrayAdapter(this@OnboardingActivity, android.R.layout.simple_list_item_1, colleges)
                )
            }
        }
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener { handleNavigation() }
        binding.btnScanCard.setOnClickListener {
            Toast.makeText(this, R.string.onboarding_scan_toast, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleNavigation() {
        when (currentStep) {
            1 -> {
                val country = binding.dropdownCountry.text.toString()
                if (country.isNotEmpty()) showCollegeStep()
                else Toast.makeText(this, R.string.onboarding_error_country, Toast.LENGTH_SHORT).show()
            }
            2 -> {
                val college = binding.dropdownCollege.text.toString()
                if (college.isNotEmpty()) showVerificationStep()
                else Toast.makeText(this, R.string.onboarding_error_college, Toast.LENGTH_SHORT).show()
            }
            3 -> {
                val id = binding.editCollegeId.text.toString()
                if (id.isNotEmpty()) completeOnboarding(id)
                else Toast.makeText(this, R.string.onboarding_error_id, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCollegeStep() {
        currentStep = 2
        binding.onboardingProgress.progress = 66
        binding.stepText.text = getString(R.string.onboarding_step_2)
        binding.onboardingTitle.text = getString(R.string.onboarding_title_step_2)
        binding.onboardingSubtitle.text = getString(R.string.onboarding_subtitle_step_2)
        binding.layoutCountry.visibility = View.GONE
        binding.layoutCollege.visibility = View.VISIBLE
    }

    private fun showVerificationStep() {
        currentStep = 3
        binding.onboardingProgress.progress = 100
        binding.stepText.text = getString(R.string.onboarding_step_3)
        binding.onboardingTitle.text = getString(R.string.onboarding_title_step_3)
        binding.onboardingSubtitle.text = getString(R.string.onboarding_subtitle_step_3)
        binding.layoutCollege.visibility = View.GONE
        binding.layoutIdVerification.visibility = View.VISIBLE
        binding.btnNext.text = getString(R.string.onboarding_btn_finish)
    }

    private fun completeOnboarding(studentId: String) {
        binding.verifyingOverlay.visibility = View.VISIBLE
        binding.btnNext.isEnabled = false

        val repository = (application as EventApplication).repository
        val selectedCollege = binding.dropdownCollege.text.toString()

        // Build synthetic credentials from student ID
        val email    = "$studentId@campus.edu"
        val password = studentId
        val fullName = "Student $studentId"

        lifecycleScope.launch {
            try {
                // Try to register first; if the user exists, just login
                try {
                    repository.register(email, password, fullName)
                } catch (_: Exception) {
                    // Possibly already registered – proceed to login
                }

                repository.login(email, password)

                // Update profile with college name
                try {
                    repository.updateCurrentUser(
                        com.example.eventapp.network.models.UserUpdateRequest(
                            collegeName = selectedCollege
                        )
                    )
                } catch (e: Exception) {
                    // Ignore failure of profile update if login succeeded
                }

                // Mark verified in shared prefs
                getSharedPreferences("CampusLinkPrefs", MODE_PRIVATE)
                    .edit { putBoolean("is_verified", true) }

                startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                finish()

            } catch (e: Exception) {
                binding.verifyingOverlay.visibility = View.GONE
                binding.btnNext.isEnabled = true
                Toast.makeText(
                    this@OnboardingActivity,
                    "Verification failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
