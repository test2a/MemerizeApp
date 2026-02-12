/*******************************************************************************
Created By Suhas Dissanayake on 7/9/23, 3:34 PM
Copyright (c) 2023
https://github.com/test2a/
All Rights Reserved
 ******************************************************************************/

package app.test2a.memerize.backend.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.test2a.memerize.utils.UpdateUtil
import kotlinx.coroutines.launch

class CheckUpdateViewModel : ViewModel() {
    var latestVersion: Float? by mutableStateOf(null)
    val currentVersion = UpdateUtil.currentVersion

    init {
        getLatestRelease()
    }

    private fun getLatestRelease() {
        viewModelScope.launch {
            latestVersion = UpdateUtil.getLatestVersion()
        }
    }
}
