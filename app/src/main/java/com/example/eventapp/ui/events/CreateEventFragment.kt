package com.example.eventapp.ui.events

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
import com.example.eventapp.databinding.FragmentCreateEventBinding

class CreateEventFragment : Fragment() {

    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventsViewModel: EventsViewModel

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
    }

    private fun publishEvent(title: String, description: String, location: String) {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.btnPublish.isEnabled = false
        
        eventsViewModel.createEvent(title, description, location) { success, message ->
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
