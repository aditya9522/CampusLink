package com.example.eventapp.ui.events

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
import androidx.navigation.findNavController
import com.example.eventapp.AppViewModelFactory
import com.example.eventapp.EventApplication
import com.example.eventapp.R
import com.example.eventapp.data.Event
import com.example.eventapp.databinding.FragmentEventsBinding
import com.example.eventapp.databinding.ItemEventBinding
import org.json.JSONObject

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventsViewModel: EventsViewModel
    private lateinit var adapter: EventsAdapter

    // Currently active category filter ("All", "Fests", "Workshops", "Sports")
    private var activeFilter = "All"
    private var searchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val repository = (requireActivity().application as EventApplication).repository
        val factory = AppViewModelFactory(repository)
        eventsViewModel = ViewModelProvider(this, factory)[EventsViewModel::class.java]

        _binding = FragmentEventsBinding.inflate(inflater, container, false)

        adapter = EventsAdapter { event ->
            eventsViewModel.registerForEvent(event.id) { success, msg ->
                // Parse JSON detail error if needed
                val displayMsg = if (!success) {
                    tryParseDetailMessage(msg) ?: msg
                } else msg
                Toast.makeText(context, displayMsg, Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerviewEvents.adapter = adapter

        eventsViewModel.events.observe(viewLifecycleOwner) {
            applyFilters(it)
        }

        eventsViewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                val displayMsg = tryParseDetailMessage(error) ?: error
                Toast.makeText(context, displayMsg, Toast.LENGTH_SHORT).show()
            }
        }

        // ── Filter chips ─────────────────────────────────────────────────────
        binding.chipGroupFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            activeFilter = when {
                checkedIds.contains(R.id.chip_fests) -> "Fests"
                checkedIds.contains(R.id.chip_workshops) -> "Workshops"
                checkedIds.contains(R.id.chip_sports) -> "Sports"
                else -> "All"
            }
            applyFilters(eventsViewModel.events.value ?: emptyList())
        }

        // ── Search bar ───────────────────────────────────────────────────────
        binding.editSearchEvents.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                applyFilters(eventsViewModel.events.value ?: emptyList())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ── Tab layout ───────────────────────────────────────────────────────
        binding.eventsTabLayout.addOnTabSelectedListener(object :
            com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                if (tab?.position == 1) {
                    binding.searchFilterContainer.visibility = View.GONE
                    binding.recyclerviewEvents.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                } else {
                    binding.searchFilterContainer.visibility = View.VISIBLE
                    binding.recyclerviewEvents.visibility = View.VISIBLE
                    binding.layoutEmptyState.visibility = View.GONE
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        binding.btnBrowseEvents.setOnClickListener {
            binding.eventsTabLayout.getTabAt(0)?.select()
        }

        return binding.root
    }

    private fun applyFilters(allEvents: List<Event>) {
        val filtered = allEvents.filter { event ->
            val matchesSearch = searchQuery.isEmpty() ||
                event.title.contains(searchQuery, ignoreCase = true) ||
                event.location.contains(searchQuery, ignoreCase = true)

            val matchesCategory = activeFilter == "All" ||
                event.category.contains(activeFilter, ignoreCase = true) ||
                event.title.contains(activeFilter, ignoreCase = true)

            matchesSearch && matchesCategory
        }
        adapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class EventsAdapter(private val onRegister: (Event) -> Unit) :
        ListAdapter<Event, EventsViewHolder>(object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
                oldItem == newItem
        }) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
            val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return EventsViewHolder(binding)
        }

        override fun onBindViewHolder(holder: EventsViewHolder, position: Int) {
            val event = getItem(position)
            val context = holder.root.context
            holder.title.text = event.title
            holder.details.text = context.getString(R.string.event_details_format, event.date, event.location)
            holder.liveBadge.visibility = if (position % 2 == 0) View.VISIBLE else View.GONE
            holder.buddyText.text =
                context.getString(R.string.label_buddy_finding_format, (10..50).random())

            holder.findBuddyBtn.setOnClickListener {
                Toast.makeText(
                    context,
                    context.getString(R.string.event_finding_buddy_toast, event.title),
                    Toast.LENGTH_SHORT
                ).show()
                holder.findBuddyBtn.findNavController().navigate(R.id.nav_chat)
            }

            holder.root.setOnClickListener {
                onRegister(event)
            }
        }
    }

    class EventsViewHolder(binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val root = binding.root
        val title: TextView = binding.eventTitle
        val details: TextView = binding.eventDetails
        val liveBadge: View = binding.eventLiveBadge
        val findBuddyBtn = binding.btnFindBuddy
        val buddyText: TextView = binding.buddyText
    }

    companion object {
        fun tryParseDetailMessage(raw: String): String? {
            return try {
                val json = JSONObject(raw)
                json.optString("detail").takeIf { it.isNotBlank() }
                    ?: json.optString("message").takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                null
            }
        }
    }
}
