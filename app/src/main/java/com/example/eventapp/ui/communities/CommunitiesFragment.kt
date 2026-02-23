package com.example.eventapp.ui.communities

import android.os.Bundle
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val repository = (requireActivity().application as EventApplication).repository
        val factory = AppViewModelFactory(repository)
        val communitiesViewModel = ViewModelProvider(this, factory)[CommunitiesViewModel::class.java]
        _binding = FragmentCommunitiesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView = binding.recyclerviewCommunities
        val adapter = CommunitiesAdapter()
        recyclerView.adapter = adapter
        communitiesViewModel.communities.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        return root
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
            holder.members.text = context.getString(R.string.community_members_count, community.memberCount)
            holder.category.text = if (community.name.contains("year", true) || community.name.contains("Batch", true)) {
                context.getString(R.string.community_category_branch)
            } else {
                context.getString(R.string.community_category_interest)
            }
            holder.activeDot.visibility = if (position % 2 == 0) View.VISIBLE else View.INVISIBLE

            holder.joinButton.setOnClickListener {
                android.widget.Toast.makeText(context, context.getString(R.string.community_joining_toast, community.name), android.widget.Toast.LENGTH_SHORT).show()
                // Navigate to Chat
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
