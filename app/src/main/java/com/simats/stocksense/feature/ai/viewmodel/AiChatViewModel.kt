package com.simats.stocksense.feature.ai.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.data.model.AiHistoryItem
import com.simats.stocksense.data.model.ChatMessage
import com.simats.stocksense.data.repository.AiRepository
import com.simats.stocksense.feature.ai.AiChatStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AiChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val showStillThinking: Boolean = false,
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

    private var thinkingJob: Job? = null

    fun clearChat() {
        thinkingJob?.cancel()
        thinkingJob = null
        AiChatStore.clear()
        _uiState.update {
            it.copy(
                messages = emptyList(),
                inputText = "",
                isTyping = false,
                showStillThinking = false,
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
            val langHint = when (_uiState.value.selectedLanguage) {
                "ta-IN" -> "Tamil"
                "hi-IN" -> "Hindi"
                else -> "English"
            }
            sendMessage(rawMessage = text, languageHint = langHint)
        }
    }

    fun retryQuestion(question: String) {
        sendMessage(rawMessage = question, languageHint = null)
    }

    fun sendMessage(rawMessage: String, languageHint: String? = null) {
        val trimmed = rawMessage.trim()
        if (trimmed.isBlank() || _uiState.value.isTyping) return

        // Take up to 6 non-error prior messages for history context (role + text only, no cards)
        val priorHistory = _uiState.value.messages
            .filter { !it.isError && it.text.isNotBlank() }
            .takeLast(6)
            .map { msg ->
                AiHistoryItem(
                    role = if (msg.isUser) "user" else "assistant",
                    text = msg.text.take(300)
                )
            }

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
                isTyping = true,
                showStillThinking = false
            )
        }

        thinkingJob?.cancel()
        thinkingJob = viewModelScope.launch {
            delay(8000L)
            if (_uiState.value.isTyping) {
                _uiState.update { it.copy(showStillThinking = true) }
            }
        }

        viewModelScope.launch {
            val result = repository.chat(
                message = trimmed,
                languageHint = languageHint,
                history = if (priorHistory.isEmpty()) null else priorHistory
            )
            thinkingJob?.cancel()
            thinkingJob = null

            result.onSuccess { data ->
                val assistantMessage = ChatMessage(
                    isUser = false,
                    text = data.answer,
                    cards = data.cards ?: emptyList(),
                    actions = data.actions ?: emptyList(),
                    suggestions = data.suggestions ?: emptyList()
                )
                val updatedWithAssistant = _uiState.value.messages + assistantMessage
                AiChatStore.setMessages(updatedWithAssistant)
                _uiState.update { current ->
                    current.copy(
                        messages = updatedWithAssistant,
                        isTyping = false,
                        showStillThinking = false
                    )
                }
            }.onFailure { error ->
                val isTimeout = error is java.net.SocketTimeoutException ||
                    error is java.io.InterruptedIOException ||
                    error.message?.contains("timeout", ignoreCase = true) == true ||
                    error.message?.contains("timed out", ignoreCase = true) == true

                val errorMsg = when {
                    isTimeout -> "Dukaan Mitra took too long to answer. Please check your connection and tap Retry."
                    error.message?.contains("Session expired", ignoreCase = true) == true -> "Session expired. Please log in again."
                    else -> error.message ?: "Could not get an answer right now."
                }
                val is401 = errorMsg.contains("Session expired", ignoreCase = true)

                if (is401) {
                    _uiState.update {
                        it.copy(
                            isSessionExpired = true,
                            isTyping = false,
                            showStillThinking = false
                        )
                    }
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
                            isTyping = false,
                            showStillThinking = false
                        )
                    }
                }
            }
        }
    }
}
