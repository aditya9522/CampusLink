package com.example.eventapp.ui.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.eventapp.R
import com.example.eventapp.databinding.FragmentScannerBinding

class ScannerFragment : Fragment() {

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        
        binding.btnScan.setOnClickListener {
            simulateScan()
        }
        
        return binding.root
    }

    private fun simulateScan() {
        binding.btnScan.isEnabled = false
        binding.scannerHint.text = getString(R.string.scanner_processing)
        
        binding.root.postDelayed({
            showSuccessDialog()
            binding.btnScan.isEnabled = true
            binding.scannerHint.text = getString(R.string.scanner_hint_default)
        }, 1500)
    }

    private fun showSuccessDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.scanner_success_title)
            .setMessage(R.string.scanner_success_message)
            .setPositiveButton(R.string.scanner_btn_awesome) { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.checkbox_on_background)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
