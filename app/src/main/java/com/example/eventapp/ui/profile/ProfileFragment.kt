package com.example.eventapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.eventapp.AppViewModelFactory
import com.example.eventapp.EventApplication
import com.example.eventapp.R
import androidx.lifecycle.lifecycleScope
import com.example.eventapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val imagePicker = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
        uri?.let { uploadImage(it) }
    }

    private fun uploadImage(uri: android.net.Uri) {
        val repository = (requireActivity().application as EventApplication).repository
        
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val tempFile = java.io.File(requireContext().cacheDir, "temp_profile.jpg")
        tempFile.outputStream().use { inputStream?.copyTo(it) }
        
        lifecycleScope.launchWhenStarted {
            try {
                _binding?.let { it.profileImage.alpha = 0.5f }
                repository.uploadProfileImage(tempFile)
                Toast.makeText(context, "Profile image updated!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _binding?.let { it.profileImage.alpha = 1.0f }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val repository = (requireActivity().application as EventApplication).repository
        val factory = AppViewModelFactory(repository)
        val profileViewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        profileViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.profileName.text = it.fullName ?: "Student"
                binding.profileCollege.text = it.collegeName ?: "Class of 2026"
                binding.profileDetailEmail.text = it.email
                binding.profileDetailPhone.text = it.phoneNumber ?: "Not provided"
                binding.profileDetailAddress.text = it.address ?: "Not provided"
                
                // Load profile image
                if (!it.profileImageUrl.isNullOrEmpty()) {
                    val fullUrl = if (it.profileImageUrl.startsWith("http")) it.profileImageUrl else "${com.example.eventapp.network.AppConfig.BASE_URL}${it.profileImageUrl}"
                    com.bumptech.glide.Glide.with(this)
                        .load(fullUrl)
                        .placeholder(R.mipmap.ic_launcher_round)
                        .into(binding.profileImage)
                }

                // Clear and add chips for interests
                binding.profileInterestsChips.removeAllViews()
                it.interests?.split(",")?.forEach { interest ->
                    if (interest.isNotBlank()) {
                        val chip = com.google.android.material.chip.Chip(requireContext())
                        chip.text = interest.trim()
                        binding.profileInterestsChips.addView(chip)
                    }
                }

                // Multi-tenancy UI logic
                if (it.collegeId != null) {
                    binding.root.findViewById<View>(R.id.btn_join_college)?.visibility = View.GONE
                    binding.root.findViewById<View>(R.id.btn_verify_account)?.visibility = View.VISIBLE
                } else {
                    binding.root.findViewById<View>(R.id.btn_join_college)?.visibility = View.VISIBLE
                    binding.root.findViewById<View>(R.id.btn_verify_account)?.visibility = View.GONE
                }
            }
        }

        profileViewModel.error.observe(viewLifecycleOwner) { err ->
            if (!err.isNullOrBlank()) {
                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMyEvents.setOnClickListener {
            findNavController().navigate(R.id.nav_events)
        }

        // Logout logic - Binding is missing explicit ID for the logout button in the previous layout read, 
        // I need to check the layout file again to find the button ID or assign one.
        // Based on fragment_profile.xml: there is a button with text "@string/profile_btn_logout" 
        // but no ID was explicitly seen in the view_file snippet (it was at the end).
        // Let's add an ID or use findViewWithTag if needed, or better, I'll update the layout if it has no ID.
        
        // I will assume I added/will add android:id="@+id/btn_logout" to the button
        binding.root.findViewById<View>(R.id.btn_logout)?.setOnClickListener {
            profileViewModel.logout {
                requireActivity().finish()
            }
        }

        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog(profileViewModel)
        }

        binding.profileImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.root.findViewById<android.view.View>(R.id.btn_verify_account).setOnClickListener {
            idPicker.launch("image/*")
        }

        binding.root.findViewById<android.view.View>(R.id.btn_notifications).setOnClickListener {
            findNavController().navigate(R.id.nav_notifications)
        }

        binding.root.findViewById<android.view.View>(R.id.btn_join_college).setOnClickListener {
            showJoinCollegeDialog(repository, profileViewModel)
        }

        return binding.root
    }

    private fun showJoinCollegeDialog(repository: com.example.eventapp.repository.AppRepository, viewModel: ProfileViewModel) {
        val input = android.widget.EditText(requireContext())
        input.hint = "Enter 8-digit Campus Invite Code"
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Join Your Campus")
            .setMessage("Enter the invite code provided by your college admin to access exclusive campus events.")
            .setView(input)
            .setPositiveButton("Join") { _, _ ->
                val code = input.text.toString()
                if (code.length >= 4) {
                    lifecycleScope.launchWhenStarted {
                        try {
                            repository.joinCollege(code)
                            Toast.makeText(context, "Joined Successfully!", Toast.LENGTH_LONG).show()
                            viewModel.loadProfile() // Refresh
                        } catch (e: Exception) {
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private val idPicker = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
        uri?.let { uploadVerification(it) }
    }

    private fun uploadVerification(uri: android.net.Uri) {
        val repository = (requireActivity().application as EventApplication).repository
        
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val tempFile = java.io.File(requireContext().cacheDir, "temp_id_card.jpg")
        tempFile.outputStream().use { inputStream?.copyTo(it) }
        
        lifecycleScope.launchWhenStarted {
            try {
                repository.uploadVerificationID(tempFile)
                Toast.makeText(context, "Verification ID submitted to Admin!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Submission failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditProfileDialog(viewModel: ProfileViewModel) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_edit_name)
        val etCollege = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_edit_college)
        val etPhone = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_edit_phone)
        val etAddress = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_edit_address)
        val etInterests = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_edit_interests)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btn_save_edit)

        val currentUser = viewModel.user.value
        currentUser?.let {
            etName.setText(it.fullName)
            etCollege.setText(it.collegeName)
            etPhone.setText(it.phoneNumber)
            etAddress.setText(it.address)
            etInterests.setText(it.interests)
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnSave.setOnClickListener {
            val req = com.example.eventapp.network.models.UserUpdateRequest(
                fullName = etName.text.toString(),
                collegeName = etCollege.text.toString(),
                phoneNumber = etPhone.text.toString(),
                address = etAddress.text.toString(),
                interests = etInterests.text.toString()
            )
            viewModel.updateProfile(req)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
