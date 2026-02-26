package com.example.eventapp.ui.marketplace

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventapp.EventApplication
import com.example.eventapp.R
import com.example.eventapp.databinding.FragmentMarketplaceBinding
import com.example.eventapp.network.AppConfig
import com.example.eventapp.network.models.MarketplaceItemResponse
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class MarketplaceFragment : Fragment() {

    private var _binding: FragmentMarketplaceBinding? = null
    private val binding get() = _binding!!

    private var allItems: List<MarketplaceItemResponse> = emptyList()
    private lateinit var adapter: MarketplaceAdapter
    private var searchQuery = ""
    private var activeCategory = "All"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketplaceBinding.inflate(inflater, container, false)

        adapter = MarketplaceAdapter(emptyList())
        binding.rvMarketplace.layoutManager = LinearLayoutManager(context)
        binding.rvMarketplace.adapter = adapter

        loadItems()

        binding.fabAddItem.setOnClickListener {
            Toast.makeText(context, "Add item coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.editSearchMarketplace.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    private fun loadItems() {
        val repository = (requireActivity().application as EventApplication).repository
        lifecycleScope.launch {
            try {
                allItems = repository.getMarketplaceItems()
                applyFilters()
            } catch (e: Exception) {
                val msg = parseDetailMessage(e.message) ?: e.message ?: "Failed to load items"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyFilters() {
        val filtered = allItems.filter { item ->
            val matchesSearch = searchQuery.isEmpty() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true) ||
                (item.ownerName?.contains(searchQuery, ignoreCase = true) == true)

            val matchesCategory = activeCategory == "All" ||
                item.category.contains(activeCategory, ignoreCase = true)

            matchesSearch && matchesCategory
        }
        adapter.updateItems(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun parseDetailMessage(raw: String?): String? {
            if (raw.isNullOrBlank()) return null
            return try {
                JSONObject(raw).optString("detail").takeIf { it.isNotBlank() }
            } catch (e: JSONException) { null }
        }
    }
}

class MarketplaceAdapter(private var items: List<MarketplaceItemResponse>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<MarketplaceAdapter.ViewHolder>() {

    fun updateItems(newItems: List<MarketplaceItemResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

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
            val fullUrl = if (item.imageUrl.startsWith("http")) item.imageUrl
            else "${AppConfig.BASE_URL}${item.imageUrl}"
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(fullUrl)
                .placeholder(R.mipmap.ic_launcher_round)
                .into(holder.image)
        }
    }

    override fun getItemCount() = items.size
}
