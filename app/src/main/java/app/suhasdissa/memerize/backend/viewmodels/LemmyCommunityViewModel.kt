/*******************************************************************************
Created By Suhas Dissanayake on 7/30/23, 2:18 PM
Copyright (c) 2023
https://github.com/test2a/
All Rights Reserved
 ******************************************************************************/

package app.test2a.memerize.backend.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.test2a.memerize.MemerizeApplication
import app.test2a.memerize.backend.database.entity.LemmyCommunity
import app.test2a.memerize.backend.repositories.LemmyCommunityRepository
import app.test2a.memerize.backend.viewmodels.state.AboutCommunityState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LemmyCommunityViewModel(private val lemmyRepository: LemmyCommunityRepository) :
    ViewModel() {

    val communities = lemmyRepository.getCommunities().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = listOf()
    )

    var aboutCommutnityState: AboutCommunityState by mutableStateOf(
        AboutCommunityState.Loading(
            LemmyCommunity("", "")
        )
    )

    fun removeCommunity(community: LemmyCommunity) {
        viewModelScope.launch {
            lemmyRepository.removeCommunity(community)
        }
    }

    fun getInfo(instance: String, community: String) {
        viewModelScope.launch {
            aboutCommutnityState = AboutCommunityState.Loading(LemmyCommunity(community, instance))
            val lemmyInfo = lemmyRepository.getCommunityInfo(LemmyCommunity(community, instance))
            if (lemmyInfo == null) {
                aboutCommutnityState =
                    AboutCommunityState.Error(LemmyCommunity(community, instance))
            } else {
                aboutCommutnityState = AboutCommunityState.Success(lemmyInfo)
                lemmyRepository.insertCommunity(lemmyInfo)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MemerizeApplication)
                val lemmyRepository = application.container.lemmyCommunityRepository
                LemmyCommunityViewModel(lemmyRepository)
            }
        }
    }
}
