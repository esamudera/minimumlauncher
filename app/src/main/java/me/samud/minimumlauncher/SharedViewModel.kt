package me.samud.minimumlauncher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    var isAppBarCollapsed: Boolean = false

    private val _favoritesChanged = MutableLiveData<Boolean>()
    val favoritesChanged: LiveData<Boolean> = _favoritesChanged

    fun notifyFavoritesChanged() {
        _favoritesChanged.value = true
    }

    fun onFavoritesChangedHandled() {
        _favoritesChanged.value = false
    }
}
