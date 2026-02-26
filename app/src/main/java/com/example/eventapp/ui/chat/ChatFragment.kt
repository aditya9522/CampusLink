package com.example.eventapp.ui.chat

import android.os.Bundle
import android.view.Gravity
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
import com.example.eventapp.data.ChatMessage
import com.example.eventapp.databinding.FragmentChatBinding
import com.example.eventapp.databinding.ItemChatBubbleBinding
import com.google.android.material.card.MaterialCardView

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val repository = (requireActivity().application as EventApplication).repository
        val factory = AppViewModelFactory(repository)
        chatViewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]

        _binding = FragmentChatBinding.inflate(inflater, container, false)

        val adapter = ChatAdapter()
        binding.chatRecycler.adapter = adapter

        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages)
            if (messages.isNotEmpty()) {
                binding.chatRecycler.postDelayed({
                    binding.chatRecycler.smoothScrollToPosition(messages.size - 1)
                }, 100)
            }
        }

        chatViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        chatViewModel.loadMessages()

        binding.btnSend.setOnClickListener {
            val text = binding.editMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                chatViewModel.sendMessage(text)
                binding.editMessage.text.clear()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ChatAdapter : ListAdapter<ChatMessage, ChatViewHolder>(object : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem == newItem
    }) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val binding = ItemChatBubbleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ChatViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            val msg = getItem(position)
            val ctx = holder.itemView.context

            holder.text.text = msg.text
            holder.time.text = formatTimestamp(msg.timestamp)

            val params = holder.messageCard.layoutParams as android.widget.LinearLayout.LayoutParams

            if (msg.isMe) {
                // Hide sender name for own messages
                holder.sender.visibility = View.GONE
                params.gravity = Gravity.END
                params.marginStart = 128
                params.marginEnd = 0
                holder.messageCard.layoutParams = params

                // "Sent" bubble: primary color
                holder.messageCard.setCardBackgroundColor(ctx.getColor(R.color.primaryColor))
                holder.text.setTextColor(ctx.getColor(R.color.white))
                holder.time.setTextColor(ctx.getColor(android.R.color.white))
                holder.time.alpha = 0.75f
            } else {
                // Show sender name for others
                holder.sender.visibility = View.VISIBLE
                holder.sender.text = msg.senderName
                params.gravity = Gravity.START
                params.marginStart = 0
                params.marginEnd = 128
                holder.messageCard.layoutParams = params

                // "Received" bubble: surface card, theme-aware
                holder.messageCard.setCardBackgroundColor(ctx.getColor(R.color.surface))
                holder.text.setTextColor(ctx.getColor(R.color.textPrimary))
                holder.sender.setTextColor(ctx.getColor(R.color.primaryColor))
                holder.time.setTextColor(ctx.getColor(R.color.textSecondary))
                holder.time.alpha = 0.75f
            }
        }

        private fun formatTimestamp(raw: String): String {
            return try {
                // raw could be "HH:mm:ss" or a full ISO datetime
                if (raw.contains("T")) {
                    // ISO: 2024-03-15T14:30:00
                    val parts = raw.split("T")
                    val timePart = parts[1].take(5) // HH:mm
                    timePart
                } else if (raw.length >= 5) {
                    raw.take(5) // HH:mm
                } else {
                    raw
                }
            } catch (e: Exception) {
                raw
            }
        }
    }

    class ChatViewHolder(binding: ItemChatBubbleBinding) : RecyclerView.ViewHolder(binding.root) {
        val text: TextView = binding.messageText
        val sender: TextView = binding.senderName
        val time: TextView = binding.messageTime
        val messageCard: MaterialCardView = binding.messageCard
    }
}
