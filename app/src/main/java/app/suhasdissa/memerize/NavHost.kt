/*******************************************************************************
Created By Suhas Dissanayake on 11/23/22, 4:16 PM
Copyright (c) 2022
https://github.com/test2a/
All Rights Reserved
 ******************************************************************************/

package app.test2a.memerize

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.test2a.memerize.ui.screens.home.CommunityScreen
import app.test2a.memerize.ui.screens.home.HomeScreen
import app.test2a.memerize.ui.screens.home.SubredditScreen
import app.test2a.memerize.ui.screens.primary.LemmyMemeScreen
import app.test2a.memerize.ui.screens.primary.RedditMemeScreen
import app.test2a.memerize.ui.screens.secondary.LemmyMemeFeed
import app.test2a.memerize.ui.screens.secondary.RedditMemeFeed
import app.test2a.memerize.ui.screens.settings.AboutScreen
import app.suhasdissa.memerize.ui.screens.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    onDrawerOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route,
        modifier = modifier
    ) {
        composable(route = Destination.Home.route) {
            HomeScreen(
                onNavigate = { destination ->
                    navController.navigateTo(destination.route)
                },
                onDrawerOpen
            )
        }
        composable(route = Destination.Settings.route) {
            SettingsScreen(
                onDrawerOpen,
                onAboutClick = {
                    navController.navigateTo(Destination.About.route)
                }
            )
        }
        composable(route = Destination.Subreddits.route) {
            SubredditScreen(onDrawerOpen)
        }
        composable(route = Destination.Communities.route) {
            CommunityScreen(onDrawerOpen)
        }
        composable(route = Destination.About.route) {
            AboutScreen()
        }
        composable(
            route = Destination.RedditMemeView.route
        ) {
            RedditMemeScreen(
                onClickCard = { id ->
                    navController.navigateTo("${Destination.RedditFeed.route}/$id")
                }
            )
        }
        composable(
            route = Destination.LemmyMemeView.route
        ) {
            LemmyMemeScreen(
                onClickCard = { id ->
                    navController.navigateTo("${Destination.LemmyFeed.route}/$id")
                }
            )
        }
        composable(
            route = Destination.RedditFeed.routeWithArgs,
            arguments = Destination.RedditFeed.arguments
        ) {
            val id = it.arguments?.getInt("id")
            if (id != null) {
                RedditMemeFeed(initialPage = id)
            }
        }

        composable(
            route = Destination.LemmyFeed.routeWithArgs,
            arguments = Destination.LemmyFeed.arguments
        ) {
            val id = it.arguments?.getInt("id")
            if (id != null) {
                LemmyMemeFeed(initialPage = id)
            }
        }
    }
}

fun NavHostController.navigateTo(route: String) = this.navigate(route) {
    launchSingleTop = true
    restoreState = true
}
