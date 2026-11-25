package com.simats.stocksense.feature.avatar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.stocksense.core.avatar.AvatarCatalog
import com.simats.stocksense.core.avatar.AvatarCode
import com.simats.stocksense.core.avatar.AvatarOptionRow
import com.simats.stocksense.core.avatar.AvatarStyle
import com.simats.stocksense.core.network.NetworkClient
import com.simats.stocksense.core.network.SessionManager
import com.simats.stocksense.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AvatarEditorStep {
    CHOOSE_AVATAR,
    STYLE_IT
}

data class AvatarCustomizerUiState(
    val step: AvatarEditorStep = AvatarEditorStep.CHOOSE_AVATAR,
    val activeStyle: AvatarStyle = AvatarStyle.OPEN_PEEPS,
    val selectedGridSeed: String? = null,
    val draftAvatar: AvatarCode = AvatarCode(
        style = AvatarStyle.OPEN_PEEPS.id,
        seed = AvatarCatalog.BASE_SEEDS_BY_STYLE[AvatarStyle.OPEN_PEEPS]!!.first()
    ),
    val savedAvatar: AvatarCode? = null,
    val ownerName: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val showSavedSuccessDialog: Boolean = false
)

/**
 * ViewModel backing the two-step DiceBear avatar picker and customizer flow.
 */
class AvatarCustomizerViewModel(
    private val profileRepository: ProfileRepository = ProfileRepository(),
    customSessionManager: SessionManager? = null
) : ViewModel() {

    private val sessionManager: SessionManager? =
        customSessionManager ?: NetworkClient.getSessionManager()

    private val _uiState = MutableStateFlow(AvatarCustomizerUiState())
    val uiState: StateFlow<AvatarCustomizerUiState> = _uiState.asStateFlow()

    fun initSessionState() {
        val currentSaved = sessionManager?.getAvatar()
        val owner = sessionManager?.getUserFullName().orEmpty()
        val initialStyle = AvatarStyle.fromId(currentSaved?.style) ?: AvatarStyle.OPEN_PEEPS
        val defaultSeed = AvatarCatalog.BASE_SEEDS_BY_STYLE[initialStyle]?.first()
            ?: AvatarCatalog.generateRandomSeed()
        _uiState.value = AvatarCustomizerUiState(
            step = AvatarEditorStep.CHOOSE_AVATAR,
            activeStyle = initialStyle,
            selectedGridSeed = null,
            draftAvatar = currentSaved ?: AvatarCode(style = initialStyle.id, seed = defaultSeed),
            savedAvatar = currentSaved,
            ownerName = owner,
            isSaving = false,
            errorMessage = null,
            showSavedSuccessDialog = false
        )
    }

    fun selectStyleTab(style: AvatarStyle) {
        _uiState.update { current ->
            current.copy(
                activeStyle = style,
                selectedGridSeed = null,
                errorMessage = null
            )
        }
    }

    fun selectGridSeed(seed: String) {
        _uiState.update { current ->
            current.copy(
                selectedGridSeed = seed,
                errorMessage = null
            )
        }
    }

    fun proceedToStyleStep() {
        val state = _uiState.value
        val chosenSeed = state.selectedGridSeed ?: return
        val style = state.activeStyle
        val basePreset = AvatarCatalog.getBasePreset(style, chosenSeed)
            ?: AvatarCode(
                style = style.id,
                seed = chosenSeed,
                options = AvatarCatalog.defaultFriendlyOptions(style)
            )
        _uiState.update { current ->
            current.copy(
                step = AvatarEditorStep.STYLE_IT,
                draftAvatar = basePreset,
                errorMessage = null
            )
        }
    }

    fun customizeCurrentAvatar() {
        val saved = _uiState.value.savedAvatar ?: return
        val style = AvatarStyle.fromId(saved.style) ?: return
        _uiState.update { current ->
            current.copy(
                step = AvatarEditorStep.STYLE_IT,
                activeStyle = style,
                draftAvatar = saved,
                errorMessage = null
            )
        }
    }

    fun backToChooseStep() {
        _uiState.update { current ->
            current.copy(
                step = AvatarEditorStep.CHOOSE_AVATAR,
                errorMessage = null
            )
        }
    }

    fun updateSingleOption(optionKey: String, value: String) {
        _uiState.update { current ->
            val style = AvatarStyle.fromId(current.draftAvatar.style) ?: current.activeStyle
            val updated = current.draftAvatar.options.toMutableMap()
            updated[optionKey] = value
            current.copy(
                draftAvatar = current.draftAvatar.copy(
                    options = AvatarCatalog.sanitizeOptions(style, updated)
                ),
                errorMessage = null
            )
        }
    }

    fun updateOptionalVariant(row: AvatarOptionRow.OptionalVariantChips, selectedVariant: String?) {
        _uiState.update { current ->
            val style = AvatarStyle.fromId(current.draftAvatar.style) ?: current.activeStyle
            val updated = current.draftAvatar.options.toMutableMap()
            if (selectedVariant == null) {
                updated[row.probabilityKey] = "0"
                updated.remove(row.variantKey)
            } else {
                updated[row.probabilityKey] = "100"
                updated[row.variantKey] = selectedVariant
            }
            current.copy(
                draftAvatar = current.draftAvatar.copy(
                    options = AvatarCatalog.sanitizeOptions(style, updated)
                ),
                errorMessage = null
            )
        }
    }

    fun randomizeDraft() {
        _uiState.update { current ->
            val style = AvatarStyle.fromId(current.draftAvatar.style) ?: current.activeStyle
            current.copy(
                draftAvatar = AvatarCatalog.randomize(style),
                errorMessage = null
            )
        }
    }

    fun resetDraft() {
        _uiState.update { current ->
            val style = AvatarStyle.fromId(current.draftAvatar.style) ?: current.activeStyle
            val resetOptions = AvatarCatalog.getBasePreset(style, current.draftAvatar.seed)?.options
                ?: AvatarCatalog.defaultFriendlyOptions(style)
            current.copy(
                draftAvatar = current.draftAvatar.copy(options = resetOptions),
                errorMessage = null
            )
        }
    }

    fun saveAvatar() {
        val state = _uiState.value
        if (state.isSaving) return

        val sanitized = AvatarCatalog.parseAvatarCode(state.draftAvatar)
        if (sanitized == null) {
            _uiState.update { it.copy(errorMessage = "Please choose a valid avatar first.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = profileRepository.saveAvatar(sanitized)
            result.fold(
                onSuccess = { saved ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            savedAvatar = saved ?: sanitized,
                            draftAvatar = saved ?: sanitized,
                            errorMessage = null,
                            showSavedSuccessDialog = true
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = err.message ?: "Unable to save avatar."
                        )
                    }
                }
            )
        }
    }

    fun removeAvatar(onRemoved: () -> Unit) {
        val state = _uiState.value
        if (state.isSaving) return

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = profileRepository.removeAvatar()
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            savedAvatar = null,
                            selectedGridSeed = null,
                            errorMessage = null
                        )
                    }
                    onRemoved()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = err.message ?: "Unable to remove avatar."
                        )
                    }
                }
            )
        }
    }

    fun dismissSavedDialog() {
        _uiState.update { it.copy(showSavedSuccessDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
