package com.example.eventapp.ui.travel

import android.os.Bundle
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val repository = (requireActivity().application as EventApplication).repository
        val factory = AppViewModelFactory(repository)
        val travelViewModel = ViewModelProvider(this, factory)[TravelViewModel::class.java]
        _binding = FragmentTravelBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView = binding.recyclerviewTravel
        val adapter = TravelAdapter()
        recyclerView.adapter = adapter
        travelViewModel.travelPlans.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        return root
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
            holder.destination.text = context.getString(R.string.travel_destination_format, plan.destination)
            holder.details.text = context.getString(R.string.travel_details_format, plan.date, plan.mode, plan.seatsAvailable)
            holder.seatsLeft.text = context.getString(R.string.label_seats_left_format, plan.seatsAvailable)
            
            holder.joinButton.setOnClickListener {
                Toast.makeText(context, context.getString(R.string.travel_request_sent_toast, plan.destination), Toast.LENGTH_SHORT).show()
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
