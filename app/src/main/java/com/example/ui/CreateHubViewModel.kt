package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.TownshipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreateHubViewModel(
    private val repository: TownshipRepository,
    application: Application
) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("connectkar_prefs", Context.MODE_PRIVATE)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _recentlyUsed = MutableStateFlow<List<String>>(emptyList())
    val recentlyUsed: StateFlow<List<String>> = _recentlyUsed.asStateFlow()

    init {
        loadRecentlyUsed()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun loadRecentlyUsed() {
        // Default recently used lists as shown in HTML (Sell Product and Carpool)
        val saved = sharedPrefs.getString("recently_used", "MARKETPLACE,CARPOOL") ?: "MARKETPLACE,CARPOOL"
        _recentlyUsed.value = if (saved.isEmpty()) emptyList() else saved.split(",").filter { it.isNotEmpty() }
    }

    fun selectCategory(categoryId: String) {
        val current = _recentlyUsed.value.toMutableList()
        current.remove(categoryId)
        current.add(0, categoryId)
        val trimmed = current.take(5)
        _recentlyUsed.value = trimmed
        sharedPrefs.edit().putString("recently_used", trimmed.joinToString(",")).apply()
    }

    class Factory(
        private val repository: TownshipRepository,
        private val application: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CreateHubViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CreateHubViewModel(repository, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
