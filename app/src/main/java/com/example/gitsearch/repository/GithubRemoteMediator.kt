/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.gitsearch.repository

import android.widget.Toast
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.database.GitDatabase
import com.example.database.model.RemoteKeys
import com.example.networking.GitApi
import com.example.networking.IN_QUALIFIER
import com.example.shareddtos.Repo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException
import java.io.InvalidObjectException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

// GitHub page API is 1 based: https://developer.github.com/v3/#pagination
private const val GITHUB_STARTING_PAGE_INDEX = 1

@OptIn(ExperimentalPagingApi::class)
class GithubRemoteMediator(
    private val query: String,
    private val service: GitApi,
    private val repoDatabase: GitDatabase
) : RemoteMediator<Int, Repo>() {

    companion object {
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Repo>): MediatorResult {

        val page = when (loadType) {
            LoadType.REFRESH -> {
                GITHUB_STARTING_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                return MediatorResult.Success(endOfPaginationReached = true)
                /*
                var remoteKeys = getRemoteKeyForFirstItem(state)
                if (remoteKeys == null) {
                    // The LoadType is PREPEND so some data was loaded before,
                    // so we should have been able to get remote keys
                    // If the remoteKeys are null, then we're an invalid state and we have a bug
                    // throw InvalidObjectException("Remote key and the prevKey should not be null")
                    GITHUB_STARTING_PAGE_INDEX
                }else{
                    // If the previous key is null, then we can't request more data
                    val prevKey = remoteKeys.prevKey
                    if (prevKey == null) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
                    remoteKeys.prevKey
                }*/

            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                if (remoteKeys == null || remoteKeys.nextKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                remoteKeys.nextKey
            }

        }

        val apiQuery = query + IN_QUALIFIER

        try {
            val apiResponse = service.searchRepos(apiQuery, page!!, state.config.pageSize)
            if(apiResponse.message != null) return MediatorResult.Error(Throwable(apiResponse.message))

            if(apiResponse.total==0){
                return MediatorResult.Success(endOfPaginationReached = true)
            }
            val repos = apiResponse.items
            val endOfPaginationReached = repos.isEmpty()


            repoDatabase.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    repoDatabase.remoteKeysDao().clearRemoteKeys()
                    repoDatabase.reposDao().clearRepos()
                }
                val prevKey = if (page == GITHUB_STARTING_PAGE_INDEX) null else page.minus(1)
                val nextKey = if (endOfPaginationReached) null else page.plus(1)
                val keys = repos.map {
                    RemoteKeys(repoId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                repoDatabase.remoteKeysDao().insertAll(keys)
                repoDatabase.reposDao().insertAll(repos)
            }

            if(loadType == LoadType.PREPEND && !endOfPaginationReached && page==1)
                MediatorResult.Success(endOfPaginationReached = true)
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Repo>): RemoteKeys? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { repo ->
                // Get the remote keys of the last item retrieved
                repoDatabase.remoteKeysDao().remoteKeysRepoId(repo.id)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Repo>): RemoteKeys? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        val p = state.pages.firstOrNull{ it.data.isNotEmpty() }
        val d = p?.data?.firstOrNull()
        return d?.let { repo ->
                // Get the remote keys of the first items retrieved
                repoDatabase.remoteKeysDao().remoteKeysRepoId(repo.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Repo>
    ): RemoteKeys? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { repoId ->
                repoDatabase.remoteKeysDao().remoteKeysRepoId(repoId)
            }
        }
    }

}