package com.example.eventapp.ui.communities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eventapp.AppViewModelFactory
import com.example.eventapp.EventApplication
import com.example.eventapp.R
import com.example.eventapp.data.Community
import com.example.eventapp.databinding.FragmentCommunitiesBinding
import com.example.eventapp.databinding.ItemCommunityBinding

class CommunitiesFragment : Fragment() {

    private var _binding: FragmentCommunitiesBinding? = null
    private val binding get() = _binding!!

    private lateinit var communitiesViewModel: CommunitiesViewModel
    private lateinit var adapter: CommunitiesAdapter

    private var activeFilter = "All"
    private var searchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val repository = (requireActivity().application as EventApplication).repository
        val factory = AppViewModelFactory(repository)
        communitiesViewModel = ViewModelProvider(this, factory)[CommunitiesViewModel::class.java]
        _binding = FragmentCommunitiesBinding.inflate(inflater, container, false)

        adapter = CommunitiesAdapter()
        binding.recyclerviewCommunities.adapter = adapter

        communitiesViewModel.communities.observe(viewLifecycleOwner) {
            applyFilters(it)
        }

        // ── Filter chips ──────────────────────────────────────────────────────
        binding.chipGroupCommunities.setOnCheckedStateChangeListener { _, checkedIds ->
            activeFilter = when {
                checkedIds.contains(R.id.chip_comm_branch) -> "Branch"
                checkedIds.contains(R.id.chip_comm_interests) -> "Interests"
                else -> "All"
            }
            applyFilters(communitiesViewModel.communities.value ?: emptyList())
        }

        // ── Search ────────────────────────────────────────────────────────────
        binding.editSearchCommunities.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                applyFilters(communitiesViewModel.communities.value ?: emptyList())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    private fun applyFilters(allCommunities: List<Community>) {
        val filtered = allCommunities.filter { community ->
            val matchesSearch = searchQuery.isEmpty() ||
                community.name.contains(searchQuery, ignoreCase = true) ||
                (community.description?.contains(searchQuery, ignoreCase = true) == true)

            val isBranchType = community.name.contains("year", ignoreCase = true) ||
                community.name.contains("batch", ignoreCase = true) ||
                community.name.contains("cse", ignoreCase = true) ||
                community.name.contains("ece", ignoreCase = true) ||
                community.name.contains("mech", ignoreCase = true) ||
                community.name.contains("civil", ignoreCase = true) ||
                community.name.contains("it", ignoreCase = true) ||
                community.name.contains("branch", ignoreCase = true)

            val matchesCategory = when (activeFilter) {
                "Branch" -> isBranchType
                "Interests" -> !isBranchType
                else -> true
            }

            matchesSearch && matchesCategory
        }
        adapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class CommunitiesAdapter :
        ListAdapter<Community, CommunityViewHolder>(object : DiffUtil.ItemCallback<Community>() {
            override fun areItemsTheSame(oldItem: Community, newItem: Community): Boolean =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Community, newItem: Community): Boolean =
                oldItem == newItem
        }) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
            val binding = ItemCommunityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CommunityViewHolder(binding)
        }

        override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
            val community = getItem(position)
            val context = holder.root.context
            holder.name.text = community.name
            holder.description.text = community.description
            holder.members.text =
                context.getString(R.string.community_members_count, community.memberCount)

            val isBranch = community.name.contains("year", ignoreCase = true) ||
                community.name.contains("batch", ignoreCase = true) ||
                community.name.contains("cse", ignoreCase = true) ||
                community.name.contains("ece", ignoreCase = true) ||
                community.name.contains("branch", ignoreCase = true)

            holder.category.text = if (isBranch) {
                context.getString(R.string.community_category_branch)
            } else {
                context.getString(R.string.community_category_interest)
            }
            holder.activeDot.visibility = if (position % 2 == 0) View.VISIBLE else View.INVISIBLE

            holder.joinButton.setOnClickListener {
                android.widget.Toast.makeText(
                    context,
                    context.getString(R.string.community_joining_toast, community.name),
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                it.findNavController().navigate(R.id.nav_chat)
            }
        }
    }

    class CommunityViewHolder(binding: ItemCommunityBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val root = binding.root
        val name: TextView = binding.communityName
        val description: TextView = binding.communityDescription
        val members: TextView = binding.communityMembers
        val category: TextView = binding.communityCategory
        val activeDot: View = binding.activeIndicator
        val joinButton = binding.btnJoinRoom
    }
}
