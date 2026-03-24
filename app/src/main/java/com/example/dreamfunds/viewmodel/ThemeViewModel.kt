// app/src/main/java/com/example/dreamfunds/viewmodel/ThemeViewModel.kt
package com.example.dreamfunds.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class ThemeViewModel(app: Application) : AndroidViewModel(app) {

    private val THEME_KEY = intPreferencesKey("theme_mode")

    val themeMode: StateFlow<ThemeMode> = getApplication<Application>()
        .dataStore.data
        .map { prefs ->
            ThemeMode.entries.getOrElse(prefs[THEME_KEY] ?: 0) { ThemeMode.SYSTEM }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { prefs ->
                prefs[THEME_KEY] = mode.ordinal
            }
        }
    }
}