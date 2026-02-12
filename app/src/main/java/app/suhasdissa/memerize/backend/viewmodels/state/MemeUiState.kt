/*******************************************************************************
Created By Suhas Dissanayake on 8/4/23, 12:13 PM
Copyright (c) 2023
https://github.com/test2a/
All Rights Reserved
 ******************************************************************************/

package app.test2a.memerize.backend.viewmodels.state

import app.test2a.memerize.backend.database.entity.Meme

sealed interface MemeUiState {
    data class Success(val memes: List<Meme>) : MemeUiState
    data class Error(val error: String) : MemeUiState
    object Loading : MemeUiState
}
