package com.example.eventapp.ui.clubs

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
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eventapp.AppViewModelFactory
import com.example.eventapp.EventApplication
import com.example.eventapp.R
import com.example.eventapp.data.Club
import com.example.eventapp.databinding.FragmentClubsBinding
import com.example.eventapp.databinding.ItemClubBinding

class ClubsFragment : Fragment() {

    private var _binding: FragmentClubsBinding? = null
    private val binding get() = _binding!!

    private lateinit var clubsViewModel: ClubsViewModel
    private lateinit var adapter: ClubsAdapter

    private var activeFilter = "All"
    private var searchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val repository = (requireActivity().application as EventApplication).repository
        val factory = AppViewModelFactory(repository)
        clubsViewModel = ViewModelProvider(this, factory)[ClubsViewModel::class.java]
        _binding = FragmentClubsBinding.inflate(inflater, container, false)

        adapter = ClubsAdapter()
        binding.recyclerviewClubs.adapter = adapter

        clubsViewModel.clubs.observe(viewLifecycleOwner) {
            applyFilters(it)
        }

        // ── Category filter chips ─────────────────────────────────────────────
        binding.chipGroupClubs.setOnCheckedStateChangeListener { _, checkedIds ->
            activeFilter = when {
                checkedIds.contains(R.id.chip_club_tech) -> "Tech"
                checkedIds.contains(R.id.chip_club_design) -> "Design"
                else -> "All"
            }
            applyFilters(clubsViewModel.clubs.value ?: emptyList())
        }

        // ── Search ────────────────────────────────────────────────────────────
        binding.editSearchClubs.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                applyFilters(clubsViewModel.clubs.value ?: emptyList())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    private fun applyFilters(allClubs: List<Club>) {
        val filtered = allClubs.filter { club ->
            val matchesSearch = searchQuery.isEmpty() ||
                club.name.contains(searchQuery, ignoreCase = true) ||
                (club.description?.contains(searchQuery, ignoreCase = true) == true) ||
                (club.category?.contains(searchQuery, ignoreCase = true) == true)

            val matchesCategory = activeFilter == "All" ||
                (club.category?.contains(activeFilter, ignoreCase = true) == true) ||
                club.name.contains(activeFilter, ignoreCase = true)

            matchesSearch && matchesCategory
        }
        adapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ClubsAdapter :
        ListAdapter<Club, ClubViewHolder>(object : DiffUtil.ItemCallback<Club>() {
            override fun areItemsTheSame(oldItem: Club, newItem: Club): Boolean =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Club, newItem: Club): Boolean =
                oldItem == newItem
        }) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClubViewHolder {
            val binding = ItemClubBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ClubViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ClubViewHolder, position: Int) {
            val club = getItem(position)
            val context = holder.root.context
            holder.name.text = club.name
            holder.category.text = club.category ?: "General"
            holder.description.text = club.description

            holder.root.setOnClickListener {
                Toast.makeText(
                    context,
                    context.getString(R.string.club_opening_details_toast, club.name),
                    Toast.LENGTH_SHORT
                ).show()
            }

            holder.qrButton.setOnClickListener {
                holder.qrButton.findNavController().navigate(R.id.nav_scanner)
            }

            holder.viewDetailsButton.setOnClickListener {
                Toast.makeText(
                    context,
                    context.getString(R.string.club_recruitment_info, club.name),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    class ClubViewHolder(binding: ItemClubBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val root = binding.root
        val name: TextView = binding.clubName
        val category: TextView = binding.clubCategory
        val description: TextView = binding.clubDescription
        val qrButton = binding.btnQrCheckin
        val viewDetailsButton = binding.btnViewClub
    }
}
