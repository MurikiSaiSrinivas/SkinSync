package com.oo.skinsync.domain

/**
 * The only allowed shape for screen state (rule #6).
 * Every screen must render each case explicitly.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
