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
import com.example.eventapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

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
                // Populate profile UI fields if they exist in layout
                try { binding.root.findViewWithTag<android.widget.TextView>("tv_user_name")?.text = it.fullName ?: it.email } catch (_: Exception) {}
                try { binding.root.findViewWithTag<android.widget.TextView>("tv_user_email")?.text = it.email } catch (_: Exception) {}
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

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
