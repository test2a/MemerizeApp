/*******************************************************************************
Created By Suhas Dissanayake on 12/20/22, 8:56 AM
Copyright (c) 2022
https://github.com/SuhasDissa/
All Rights Reserved
 ******************************************************************************/

package app.suhasdissa.memerize.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun VideoCard(
    onClickVideo: (url: String) -> Unit,
    vidlink: String,
    title: String,
    preview: String,
    modifier: Modifier = Modifier
) {
    val encodedLink = URLEncoder.encode(vidlink, StandardCharsets.UTF_8.toString())
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
    ) {
        ImageCard({ onClickVideo(encodedLink) }, preview, title)
        Card(modifier.clickable(onClick = { onClickVideo(encodedLink) }), shape = CircleShape) {
            Icon(
                modifier = modifier.size(64.dp),
                imageVector = Icons.Default.PlayCircle,
                contentDescription = stringResource(
                    app.suhasdissa.memerize.R.string.play_video_hint
                )
            )
        }
    }
}

@Preview
@Composable
fun VideoCardPreview() {
    VideoCard(onClickVideo = {}, vidlink = "", title = "Preview", preview = "")
}
