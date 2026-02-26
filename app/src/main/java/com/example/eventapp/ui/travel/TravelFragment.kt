package com.example.eventapp.ui.travel

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eventapp.AppViewModelFactory
import com.example.eventapp.EventApplication
import com.example.eventapp.R
import com.example.eventapp.data.TravelPlan
import com.example.eventapp.databinding.FragmentTravelBinding
import com.example.eventapp.databinding.ItemTravelBinding

class TravelFragment : Fragment() {

    private var _binding: FragmentTravelBinding? = null
    private val binding get() = _binding!!

    private lateinit var travelViewModel: TravelViewModel
    private lateinit var adapter: TravelAdapter

    private var searchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val repository = (requireActivity().application as EventApplication).repository
        val factory = AppViewModelFactory(repository)
        travelViewModel = ViewModelProvider(this, factory)[TravelViewModel::class.java]
        _binding = FragmentTravelBinding.inflate(inflater, container, false)

        adapter = TravelAdapter()
        binding.recyclerviewTravel.adapter = adapter

        travelViewModel.travelPlans.observe(viewLifecycleOwner) {
            applyFilters(it)
        }

        travelViewModel.error.observe(viewLifecycleOwner) { err ->
            if (!err.isNullOrBlank()) {
                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            }
        }

        // ── Search bar ────────────────────────────────────────────────────────
        binding.editSearchTravel.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                applyFilters(travelViewModel.travelPlans.value ?: emptyList())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ── Post travel plan button ───────────────────────────────────────────
        binding.btnPostTravel.setOnClickListener {
            Toast.makeText(context, "Post travel plan coming soon!", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    private fun applyFilters(plans: List<TravelPlan>) {
        val filtered = if (searchQuery.isEmpty()) plans
        else plans.filter { plan ->
            plan.destination.contains(searchQuery, ignoreCase = true) ||
                plan.mode.contains(searchQuery, ignoreCase = true) ||
                plan.date.contains(searchQuery, ignoreCase = true)
        }
        adapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class TravelAdapter :
        ListAdapter<TravelPlan, TravelViewHolder>(object : DiffUtil.ItemCallback<TravelPlan>() {
            override fun areItemsTheSame(oldItem: TravelPlan, newItem: TravelPlan): Boolean =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: TravelPlan, newItem: TravelPlan): Boolean =
                oldItem == newItem
        }) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelViewHolder {
            val binding = ItemTravelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TravelViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
            val plan = getItem(position)
            val context = holder.root.context
            holder.destination.text =
                context.getString(R.string.travel_destination_format, plan.destination)
            holder.details.text =
                context.getString(R.string.travel_details_format, plan.date, plan.mode, plan.seatsAvailable)
            holder.seatsLeft.text =
                context.getString(R.string.label_seats_left_format, plan.seatsAvailable)

            holder.joinButton.setOnClickListener {
                Toast.makeText(
                    context,
                    context.getString(R.string.travel_request_sent_toast, plan.destination),
                    Toast.LENGTH_SHORT
                ).show()
                holder.joinButton.text = context.getString(R.string.travel_requested_label)
                holder.joinButton.isEnabled = false
            }
        }
    }

    class TravelViewHolder(binding: ItemTravelBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val root = binding.root
        val destination: TextView = binding.travelDestination
        val details: TextView = binding.travelDetails
        val joinButton = binding.btnJoinTravel
        val seatsLeft: TextView = binding.seatsLeftText
    }
}
