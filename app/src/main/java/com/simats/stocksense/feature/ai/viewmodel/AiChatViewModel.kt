package com.simats.stocksense.feature.ai.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.data.model.ChatMessage
import com.simats.stocksense.data.repository.AiRepository
import com.simats.stocksense.feature.ai.AiChatStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val inputText: String = "",
    val autoSpeak: Boolean = false,
    val isListeningSheetVisible: Boolean = false,
    val selectedLanguage: String = "en-IN",
    val partialTranscript: String = "",
    val speakingMessageId: String? = null,
    val isSessionExpired: Boolean = false
)

class AiChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AiRepository()
    private val prefs = application.getSharedPreferences("dukaan_mitra_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        AiChatUiState(
            messages = AiChatStore.getMessages(),
            autoSpeak = prefs.getBoolean("auto_speak_answers", false),
            selectedLanguage = prefs.getString("last_speech_language", "en-IN") ?: "en-IN"
        )
    )
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            AiChatStore.messages.collect { storedList ->
                _uiState.update { current ->
                    if (current.messages != storedList) {
                        current.copy(messages = storedList)
                    } else {
                        current
                    }
                }
            }
        }
    }

    fun clearChat() {
        AiChatStore.clear()
        _uiState.update {
            it.copy(
                messages = emptyList(),
                inputText = "",
                isTyping = false,
                speakingMessageId = null
            )
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun setAutoSpeak(enabled: Boolean) {
        prefs.edit().putBoolean("auto_speak_answers", enabled).apply()
        _uiState.update { it.copy(autoSpeak = enabled) }
    }

    fun setSelectedLanguage(lang: String) {
        prefs.edit().putString("last_speech_language", lang).apply()
        _uiState.update { it.copy(selectedLanguage = lang) }
    }

    fun showListeningSheet() {
        _uiState.update { it.copy(isListeningSheetVisible = true, partialTranscript = "") }
    }

    fun hideListeningSheet() {
        _uiState.update { it.copy(isListeningSheetVisible = false, partialTranscript = "") }
    }

    fun updatePartialTranscript(text: String) {
        _uiState.update { it.copy(partialTranscript = text) }
    }

    fun setSpeakingMessageId(id: String?) {
        _uiState.update { it.copy(speakingMessageId = id) }
    }

    fun onSpeechSend(text: String) {
        hideListeningSheet()
        if (text.isNotBlank()) {
            sendMessage(text)
        }
    }

    fun retryQuestion(question: String) {
        sendMessage(question)
    }

    fun sendMessage(rawMessage: String) {
        val trimmed = rawMessage.trim()
        if (trimmed.isBlank() || _uiState.value.isTyping) return

        val userMessage = ChatMessage(
            isUser = true,
            text = trimmed
        )

        val updatedWithUser = _uiState.value.messages + userMessage
        AiChatStore.setMessages(updatedWithUser)
        _uiState.update { current ->
            current.copy(
                messages = updatedWithUser,
                inputText = "",
                isTyping = true
            )
        }

        val langHint = when (_uiState.value.selectedLanguage) {
            "ta-IN" -> "Tamil"
            "hi-IN" -> "Hindi"
            else -> "English"
        }

        viewModelScope.launch {
            val result = repository.chat(message = trimmed, languageHint = langHint)
            result.onSuccess { data ->
                val assistantMessage = ChatMessage(
                    isUser = false,
                    text = data.answer,
                    cards = data.cards ?: emptyList(),
                    actions = data.actions ?: emptyList()
                )
                val updatedWithAssistant = _uiState.value.messages + assistantMessage
                AiChatStore.setMessages(updatedWithAssistant)
                _uiState.update { current ->
                    current.copy(
                        messages = updatedWithAssistant,
                        isTyping = false
                    )
                }
            }.onFailure { error ->
                val errorMsg = error.message ?: "Could not get an answer right now."
                val is401 = errorMsg.contains("Session expired", ignoreCase = true)

                if (is401) {
                    _uiState.update { it.copy(isSessionExpired = true, isTyping = false) }
                } else {
                    val errorMessage = ChatMessage(
                        isUser = false,
                        text = errorMsg,
                        isError = true,
                        retryQuestion = trimmed
                    )
                    val updatedWithError = _uiState.value.messages + errorMessage
                    AiChatStore.setMessages(updatedWithError)
                    _uiState.update { current ->
                        current.copy(
                            messages = updatedWithError,
                            isTyping = false
                        )
                    }
                }
            }
        }
    }
}
