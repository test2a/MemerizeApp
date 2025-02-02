/*******************************************************************************
Created By Suhas Dissanayake on 7/30/23, 1:20 PM
Copyright (c) 2023
https://github.com/SuhasDissa/
All Rights Reserved
 ******************************************************************************/

package app.suhasdissa.memerize.ui.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.suhasdissa.memerize.Destination
import app.suhasdissa.memerize.backend.database.entity.RedditCommunity
import app.suhasdissa.memerize.backend.model.SortTime
import app.suhasdissa.memerize.backend.viewmodels.LemmyCommunityViewModel
import app.suhasdissa.memerize.backend.viewmodels.LemmyViewModel
import app.suhasdissa.memerize.backend.viewmodels.RedditCommunityViewModel
import app.suhasdissa.memerize.backend.viewmodels.RedditViewModel
import app.suhasdissa.memerize.ui.components.HighlightCard

@Composable
fun HomeScreen(
    onNavigate: (Destination) -> Unit,
    redditCommunityViewModel: RedditCommunityViewModel = viewModel(
        factory = RedditCommunityViewModel.Factory
    ),
    lemmyCommunityViewModel: LemmyCommunityViewModel =
        viewModel(factory = LemmyCommunityViewModel.Factory),
    redditViewModel: RedditViewModel,
    lemmyViewModel: LemmyViewModel
) {
    val subreddits by redditCommunityViewModel.communities.collectAsState()
    val communities by lemmyCommunityViewModel.communities.collectAsState()
    val selectedSubreddits = remember { mutableStateListOf<RedditCommunity>() }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        item {
            Text("Subreddits", style = MaterialTheme.typography.titleLarge)
        }
        items(items = subreddits) {
            HighlightCard(
                onClick = {
                    if (selectedSubreddits.isNotEmpty()) {
                        redditViewModel.getMultiMemes(selectedSubreddits)
                        onNavigate(Destination.RedditMemeView)
                    } else {
                        redditViewModel.getMemePhotos(it.id, SortTime.TODAY)
                        onNavigate(Destination.RedditMemeView)
                    }
                },
                name = it.name,
                thumbnail_url = it.iconUrl,
                onLongClick = {
                    if (selectedSubreddits.contains(it)) {
                        selectedSubreddits.remove(it)
                    } else {
                        selectedSubreddits.add(it)
                    }
                },
                highlighted = selectedSubreddits.contains(it)
            )
        }
        item {
            Text("Lemmy Communities", style = MaterialTheme.typography.titleLarge)
        }
        items(items = communities) {
            HighlightCard(
                onClick = {
                    lemmyViewModel.getMemePhotos(it)
                    onNavigate(Destination.LemmyMemeView)
                },
                name = it.name,
                thumbnail_url = it.iconUrl
            )
        }
    }
}
