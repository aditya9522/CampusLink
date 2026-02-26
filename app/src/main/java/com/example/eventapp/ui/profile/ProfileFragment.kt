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
import com.example.eventapp.network.AppConfig
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileViewModel: ProfileViewModel

    private val imagePicker = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { uploadImage(it) }
    }

    private val idPicker = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { uploadVerification(it) }
    }

    private fun uploadImage(uri: android.net.Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val tempFile = java.io.File(requireContext().cacheDir, "temp_profile.jpg")
        tempFile.outputStream().use { inputStream?.copyTo(it) }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                _binding?.profileImage?.alpha = 0.5f
                // Use the ViewModel so the user LiveData auto-updates with new image URL
                profileViewModel.uploadProfileImage(tempFile)
                Toast.makeText(context, R.string.profile_image_update_success, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    getString(R.string.profile_image_update_failed, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                _binding?.profileImage?.alpha = 1.0f
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
        profileViewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        profileViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.profileName.text = it.fullName ?: getString(R.string.profile_name_default)
                binding.profileCollege.text = it.collegeName ?: getString(R.string.profile_college_default)
                binding.profileDetailEmail.text = it.email
                binding.profileDetailPhone.text = it.phoneNumber ?: getString(R.string.profile_not_provided)
                binding.profileDetailAddress.text = it.address ?: getString(R.string.profile_not_provided)

                // Load profile image with Glide
                val imageUrl = it.profileImageUrl
                if (!imageUrl.isNullOrEmpty()) {
                    val fullUrl = if (imageUrl.startsWith("http")) imageUrl
                    else "${AppConfig.BASE_URL}${imageUrl}"
                    com.bumptech.glide.Glide.with(this)
                        .load(fullUrl)
                        .placeholder(R.mipmap.ic_launcher_round)
                        .error(R.mipmap.ic_launcher_round)
                        .circleCrop()
                        .into(binding.profileImage)
                }

                // Also update the navigation drawer header if possible
                updateNavHeader(it)

                // Rebuild interest chips
                binding.profileInterestsChips.removeAllViews()
                it.interests?.split(",")?.forEach { interest ->
                    if (interest.isNotBlank()) {
                        val chip = com.google.android.material.chip.Chip(requireContext())
                        chip.text = interest.trim()
                        chip.isCheckable = false
                        binding.profileInterestsChips.addView(chip)
                    }
                }
            }
        }

        profileViewModel.error.observe(viewLifecycleOwner) { err ->
            if (!err.isNullOrBlank()) {
                // Parse JSON error body if it looks like {"detail":"..."}
                val message = tryParseDetailMessage(err) ?: err
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMyEvents.setOnClickListener {
            findNavController().navigate(R.id.nav_events)
        }

        binding.btnLogout.setOnClickListener {
            profileViewModel.logout {
                requireActivity().finish()
            }
        }

        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.profileImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.btnNotifications.setOnClickListener {
            findNavController().navigate(R.id.nav_notifications)
        }

        binding.tvVerifiedLabel.setOnClickListener {
            idPicker.launch("image/*")
        }

        return binding.root
    }

    /** Updates the navigation drawer/sidebar header with updated user info */
    private fun updateNavHeader(user: com.example.eventapp.network.models.UserResponse) {
        val activity = requireActivity() as? com.example.eventapp.MainActivity ?: return
        val navView = activity.binding.navView ?: return
        val headerView = navView.getHeaderView(0) ?: return

        headerView.findViewById<android.widget.TextView>(R.id.nav_header_name)?.text =
            user.fullName ?: getString(R.string.nav_header_user_name)
        headerView.findViewById<android.widget.TextView>(R.id.nav_header_college)?.text =
            user.collegeName ?: getString(R.string.nav_header_user_info)

        // Update sidebar profile image
        val imageUrl = user.profileImageUrl
        if (!imageUrl.isNullOrEmpty()) {
            val fullUrl = if (imageUrl.startsWith("http")) imageUrl
            else "${AppConfig.BASE_URL}${imageUrl}"
            val navImageView =
                headerView.findViewById<android.widget.ImageView>(R.id.nav_header_image)
            if (navImageView != null) {
                com.bumptech.glide.Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .circleCrop()
                    .into(navImageView)
            }
        }
    }

    private fun uploadVerification(uri: android.net.Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val tempFile = java.io.File(requireContext().cacheDir, "temp_id_card.jpg")
        tempFile.outputStream().use { inputStream?.copyTo(it) }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                profileViewModel.uploadVerificationID(tempFile) {
                    Toast.makeText(context, R.string.verification_id_submit_success, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    getString(R.string.verification_id_submit_failed, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_edit_name)
        val etCollege = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_edit_college)
        val etPhone = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_edit_phone)
        val etAddress = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_edit_address)
        val etInterests = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_edit_interests)
        val btnSave = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_save_edit)

        val currentUser = profileViewModel.user.value
        currentUser?.let {
            etName.setText(it.fullName)
            etCollege.setText(it.collegeName)
            etPhone.setText(it.phoneNumber)
            etAddress.setText(it.address)
            etInterests.setText(it.interests)
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(
            requireContext(),
            com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered
        )
            .setView(dialogView)
            .create()

        // Apply rounded corners to dialog window
        dialog.window?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            setLayout(
                (resources.displayMetrics.widthPixels * 0.92).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        btnSave.setOnClickListener {
            val req = com.example.eventapp.network.models.UserUpdateRequest(
                fullName = etName.text.toString().ifBlank { null },
                collegeName = etCollege.text.toString().ifBlank { null },
                phoneNumber = etPhone.text.toString().ifBlank { null },
                address = etAddress.text.toString().ifBlank { null },
                interests = etInterests.text.toString().ifBlank { null }
            )
            profileViewModel.updateProfile(req)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /** Parses {"detail":"..."} style error bodies into human-readable messages */
        fun tryParseDetailMessage(raw: String): String? {
            return try {
                val json = org.json.JSONObject(raw)
                json.optString("detail").takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                null
            }
        }
    }
}