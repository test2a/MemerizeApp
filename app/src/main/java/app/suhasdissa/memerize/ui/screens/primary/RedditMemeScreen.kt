/*******************************************************************************
Created By Suhas Dissanayake on 11/23/22, 4:16 PM
Copyright (c) 2022
https://github.com/SuhasDissa/
All Rights Reserved
 ******************************************************************************/

package app.suhasdissa.memerize.ui.screens.primary

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.suhasdissa.memerize.backend.viewmodels.RedditViewModel
import app.suhasdissa.memerize.backend.viewmodels.state.MemeUiState
import app.suhasdissa.memerize.ui.components.LoadingScreen
import app.suhasdissa.memerize.ui.components.MemeGrid
import app.suhasdissa.memerize.ui.components.RetryScreen

@Composable
fun RedditMemeScreen(
    modifier: Modifier = Modifier,
    redditViewModel: RedditViewModel,
    onClickMeme: (url: String) -> Unit,
    onClickVideo: (url: String) -> Unit
) {
    when (val memeDataState = redditViewModel.memeUiState) {
        is MemeUiState.Loading -> LoadingScreen(modifier)
        is MemeUiState.Error -> RetryScreen(
            "Error Loading Online Memes",
            "Show Offline Memes",
            modifier,
            onRetry = { redditViewModel.getLocalMemes() }
        )

        is MemeUiState.Success -> MemeGrid(
            memeDataState.memes,
            onClickMeme,
            onClickVideo,
            { time ->
                redditViewModel.getMemePhotos(time = time)
            },
            modifier
        )
    }
}
