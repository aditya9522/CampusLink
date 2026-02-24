package com.example.eventapp.ui.marketplace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventapp.EventApplication
import com.example.eventapp.R
import com.example.eventapp.databinding.FragmentMarketplaceBinding
import com.example.eventapp.network.models.MarketplaceItemResponse
import kotlinx.coroutines.launch

class MarketplaceFragment : Fragment() {

    private var _binding: FragmentMarketplaceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketplaceBinding.inflate(inflater, container, false)
        
        binding.rvMarketplace.layoutManager = LinearLayoutManager(context)
        loadItems()
        
        binding.fabAddItem.setOnClickListener {
            // Show add item dialog/fragment
        }

        return binding.root
    }

    private fun loadItems() {
        val repository = (requireActivity().application as EventApplication).repository
        lifecycleScope.launch {
            try {
                val items = repository.getMarketplaceItems()
                binding.rvMarketplace.adapter = MarketplaceAdapter(items)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class MarketplaceAdapter(private val items: List<MarketplaceItemResponse>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<MarketplaceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_item_title)
        val price: TextView = view.findViewById(R.id.tv_item_price)
        val owner: TextView = view.findViewById(R.id.tv_owner_name)
        val image: android.widget.ImageView = view.findViewById(R.id.iv_item_image)
        val category: TextView = view.findViewById(R.id.tv_item_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_marketplace, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.price.text = if (item.price > 0) "₹ ${item.price.toInt()}" else "FREE / LEND"
        holder.owner.text = "By ${item.ownerName ?: "Student"}"
        holder.category.text = item.category.uppercase()
        
        if (!item.imageUrl.isNullOrEmpty()) {
            val fullUrl = "https://campuslink-9wgm.onrender.com/${item.imageUrl}"
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(fullUrl)
                .into(holder.image)
        }
    }

    override fun getItemCount() = items.size
}
