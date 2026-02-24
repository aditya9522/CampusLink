package com.example.eventapp.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventapp.EventApplication
import com.example.eventapp.R
import com.example.eventapp.databinding.FragmentNotificationsBinding
import com.example.eventapp.network.models.NotificationResponse
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        
        binding.rvNotifications.layoutManager = LinearLayoutManager(context)
        loadNotifications()

        return binding.root
    }

    private fun loadNotifications() {
        val repository = (requireActivity().application as EventApplication).repository
        lifecycleScope.launch {
            try {
                val notifications = repository.getNotifications()
                binding.rvNotifications.adapter = NotificationsAdapter(notifications)
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class NotificationsAdapter(private val notifications: List<NotificationResponse>) :
    RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_notif_title)
        val message: TextView = view.findViewById(R.id.tv_notif_message)
        val time: TextView = view.findViewById(R.id.tv_notif_time)
        val icon: ImageView = view.findViewById(R.id.iv_notif_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notif = notifications[position]
        holder.title.text = notif.title
        holder.message.text = notif.message
        holder.time.text = "Just now" // Simplified logic
        
        // Change icon color based on type
        when(notif.type) {
            "success" -> holder.icon.setBackgroundResource(R.drawable.circle_green)
            "warning" -> holder.icon.setBackgroundResource(R.drawable.circle_orange)
            "error" -> holder.icon.setBackgroundResource(R.drawable.circle_red)
            else -> holder.icon.setBackgroundResource(R.drawable.circle_blue)
        }
    }

    override fun getItemCount() = notifications.size
}
