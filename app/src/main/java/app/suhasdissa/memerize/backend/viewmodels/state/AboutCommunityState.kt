/*******************************************************************************
Created By Suhas Dissanayake on 8/4/23, 9:26 PM
Copyright (c) 2023
https://github.com/test2a/
All Rights Reserved
 ******************************************************************************/

package app.test2a.memerize.backend.viewmodels.state

import app.test2a.memerize.backend.database.entity.AboutCommunity

sealed interface AboutCommunityState {
    data class Success(val community: AboutCommunity) : AboutCommunityState
    data class Error(val community: AboutCommunity) : AboutCommunityState
    data class Loading(val community: AboutCommunity) : AboutCommunityState
}
