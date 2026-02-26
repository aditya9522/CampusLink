package com.example.eventapp.ui.events

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.eventapp.AppViewModelFactory
import com.example.eventapp.EventApplication
import com.example.eventapp.R
import com.example.eventapp.databinding.FragmentCreateEventBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CreateEventFragment : Fragment() {

    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventsViewModel: EventsViewModel
    private var selectedDate: Long? = null
    private var selectedHour: Int? = null
    private var selectedMinute: Int? = null
    private var selectedImageUrl: String? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            handleImageSelection(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val repository = (requireActivity().application as EventApplication).repository
        val factory = AppViewModelFactory(repository)
        eventsViewModel = ViewModelProvider(requireActivity(), factory)[EventsViewModel::class.java]

        _binding = FragmentCreateEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPickers()

        binding.btnPublish.setOnClickListener {
            val title = binding.editEventTitle.text.toString()
            val description = binding.editEventDescription.text.toString()
            val location = binding.editEventLocation.text.toString()
            
            if (title.isNotEmpty()) {
                publishEvent(title, description, location)
            } else {
                Toast.makeText(requireContext(), R.string.create_event_error_title, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.root.findViewById<View>(R.id.dropdown_event_category).setOnClickListener {
            // Optional: Show category picker
        }
    }

    private fun setupPickers() {
        // Cover Photo
        binding.root.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_cover_photo)?.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Date Picker
        binding.editEventDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Event Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDate = selection
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = selection
                val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.editEventDate.setText(format.format(calendar.time))
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        // Time Picker
        binding.editEventTime.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Event Time")
                .build()

            timePicker.addOnPositiveButtonClickListener {
                selectedHour = timePicker.hour
                selectedMinute = timePicker.minute
                val amPm = if (timePicker.hour < 12) "AM" else "PM"
                val hour = if (timePicker.hour % 12 == 0) 12 else timePicker.hour % 12
                binding.editEventTime.setText(String.format("%02d:%02d %s", hour, timePicker.minute, amPm))
            }
            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }
    }

    private fun handleImageSelection(uri: Uri) {
        // In a real app, we'd copy the URI to a cache file and upload
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, "event_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()

        binding.loadingIndicator.visibility = View.VISIBLE
        eventsViewModel.uploadEventImage(file) { success, result ->
            binding.loadingIndicator.visibility = View.GONE
            if (success) {
                selectedImageUrl = result
                binding.imgEventPreview.visibility = View.VISIBLE
                binding.imgEventPreview.setImageURI(uri)
                binding.layoutUploadPlaceholder.visibility = View.GONE
                Toast.makeText(requireContext(), "Image uploaded!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Upload failed: $result", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun publishEvent(title: String, description: String, location: String) {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.btnPublish.isEnabled = false

        // Format start time
        var startTime: String? = null
        if (selectedDate != null && selectedHour != null) {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selectedDate!!
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour!!)
            calendar.set(Calendar.MINUTE, selectedMinute!!)
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            startTime = sdf.format(calendar.time)
        }
        
        eventsViewModel.createEvent(
            title = title,
            description = description,
            location = location,
            startTime = startTime,
            imageUrl = selectedImageUrl
        ) { success, message ->
            binding.loadingIndicator.visibility = View.GONE
            if (success) {
                binding.successOverlay.visibility = View.VISIBLE
                binding.root.postDelayed({
                    findNavController().navigateUp()
                }, 2000)
            } else {
                binding.btnPublish.isEnabled = true
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
