/*******************************************************************************
Created By Suhas Dissanayake on 11/25/22, 6:10 PM
Copyright (c) 2022
https://github.com/SuhasDissa/
All Rights Reserved
 ******************************************************************************/

package app.suhasdissa.memerize.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.suhasdissa.memerize.*
import app.suhasdissa.memerize.R
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MemerizeApp(modifier: Modifier = Modifier) {
    val navController = rememberAnimatedNavController()
    Scaffold(modifier = modifier.fillMaxSize(), topBar = {
        var showMenu by remember { mutableStateOf(false) }
        TopAppBar(title = { Text(stringResource(R.string.app_name)) }, actions = {
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert),
                    contentDescription = "More"
                )
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = {
                    Text(
                        stringResource(R.string.settings),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }, onClick = {
                    navController.navigateTo(Settings.route)
                    showMenu = false
                })
                DropdownMenuItem(text = {
                    Text(
                        stringResource(R.string.about),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }, onClick = {
                    navController.navigateTo(About.route)
                    showMenu = false
                })
            }

        })
    }) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavHost(navController = navController, modifier = Modifier)
        }
    }
}