package com.example.eventapp.ui.chat

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
            val text = binding.editMessage.text.toString()
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
            holder.text.text = msg.text
            holder.sender.text = if (msg.isMe) holder.itemView.context.getString(R.string.chat_sender_me) else msg.senderName
            holder.time.text = msg.timestamp
            
            val params = holder.messageCard.layoutParams as LinearLayout.LayoutParams
            if (msg.isMe) {
                params.gravity = Gravity.END
                holder.messageCard.setCardBackgroundColor(holder.itemView.context.getColor(R.color.primaryLightColor))
                holder.sender.setTextColor(holder.itemView.context.getColor(R.color.white))
                holder.text.setTextColor(holder.itemView.context.getColor(R.color.white))
            } else {
                params.gravity = Gravity.START
                holder.messageCard.setCardBackgroundColor(holder.itemView.context.getColor(R.color.white))
                holder.sender.setTextColor(holder.itemView.context.getColor(R.color.primaryColor))
                holder.text.setTextColor(holder.itemView.context.getColor(R.color.textPrimary))
            }
            holder.messageCard.layoutParams = params
        }
    }

    class ChatViewHolder(binding: ItemChatBubbleBinding) : RecyclerView.ViewHolder(binding.root) {
        val text: TextView = binding.messageText
        val sender: TextView = binding.senderName
        val time: TextView = binding.messageTime
        val messageCard: MaterialCardView = binding.messageCard
    }
}
