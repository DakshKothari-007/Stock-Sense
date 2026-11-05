package com.simats.stocksense.feature.ai

import com.simats.stocksense.data.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory singleton store for Dukaan Mitra AI chat messages.
 * Keeps core networking and session management decoupled from ViewModels.
 */
object AiChatStore {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun getMessages(): List<ChatMessage> = _messages.value

    fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }

    fun setMessages(messages: List<ChatMessage>) {
        _messages.value = messages
    }

    fun clear() {
        _messages.value = emptyList()
    }
}

