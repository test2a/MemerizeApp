/*******************************************************************************
Created By Suhas Dissanayake on 8/3/23, 11:40 AM
Copyright (c) 2023
https://github.com/SuhasDissa/
All Rights Reserved
 ******************************************************************************/

package app.suhasdissa.memerize.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun Player.isPlayingState(): State<PlayerState> {
    return produceState(
        initialValue = if (isPlaying) {
            PlayerState.Play
        } else if (playbackState == 2 || playbackState == 1) {
            PlayerState.Buffer
        } else {
            PlayerState.Pause
        },
        this
    ) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == 2) {
                    value = PlayerState.Buffer
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playbackState
                value = if (isPlaying) {
                    PlayerState.Play
                } else if (playbackState == 2) {
                    PlayerState.Buffer
                } else {
                    PlayerState.Pause
                }
            }
        }
        addListener(listener)
        if (!isActive) {
            removeListener(listener)
        }
    }
}

@Composable
fun Player.positionAndDurationState(): State<Pair<Long, Long?>> {
    return produceState(
        initialValue = (currentPosition to duration.let { if (it < 0) null else it }),
        this
    ) {
        var isSeeking = false
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    isSeeking = false
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                value = currentPosition to value.second
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    isSeeking = true
                    value = currentPosition to duration.let { if (it < 0) null else it }
                }
            }
        }
        addListener(listener)

        val pollJob = launch {
            while (isActive) {
                delay(1000)
                if (!isSeeking) {
                    value = currentPosition to duration.let { if (it < 0) null else it }
                }
            }
        }
        if (!isActive) {
            pollJob.cancel()
            removeListener(listener)
        }
    }
}

enum class PlayerState {
    Buffer, Play, Pause
}
