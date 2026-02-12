/*******************************************************************************
Created By Suhas Dissanayake on 11/23/22, 4:16 PM
Copyright (c) 2022
https://github.com/test2a/
All Rights Reserved
 ******************************************************************************/

package app.test2a.memerize.backend.repositories

import app.test2a.memerize.backend.database.entity.AboutCommunity
import app.test2a.memerize.backend.database.entity.Meme
import app.test2a.memerize.backend.model.Sort

interface MemeRepository<T : Meme, C : AboutCommunity> {
    suspend fun getOnlineData(community: C, sort: Sort): List<T>?
    suspend fun getLocalData(community: C): List<T>
}
