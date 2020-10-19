/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.example.gitsearch.adapter

import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import com.example.gitsearch.R
import com.example.gitsearch.features.github.GithubSearchFragmentDirections
import com.example.shareddtos.Repo
import java.time.LocalDateTime

/**
 * View Holder for a [Repo] RecyclerView list item.
 */

class RepoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val name: TextView = view.findViewById(R.id.textViewRepoName)
    private val description: TextView = view.findViewById(R.id.textViewRepoDescription)
    private val stars: TextView = view.findViewById(R.id.starCountText)
    //private val language: TextView = view.findViewById(R.id.repo_language)
    private val forks: TextView = view.findViewById(R.id.forkCountText)
    private val updated: TextView = view.findViewById(R.id.textViewRepoDate)

    private var repo: Repo? = null

    init {
        view.setOnClickListener {
            /*repo?.url?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                view.context.startActivity(intent)
            }*/

            repo?.id?.let { id ->
                val dir = GithubSearchFragmentDirections.actionGithubSearchFragmentToRepositoryDetailFragment(id)
                it.findNavController().navigate(dir)
            }
        }
    }

    fun bind(repo: Repo?) {
        if (repo == null) {
            val resources = itemView.resources
            //name.text = resources.getString(R.string.loading)
            description.visibility = View.GONE
            //anguage.visibility = View.GONE
            //stars.text = resources.getString("R.string.unknown")
            //forks.text = resources.getString(R.string.unknown)
        } else {
            showRepoData(repo)
        }
    }

    private fun showRepoData(repo: Repo) {
        this.repo = repo
        name.text = repo.fullName

        // if the description is missing, hide the TextView
        var descriptionVisibility = View.GONE
        if (repo.description != null) {
            description.text = repo.description
            descriptionVisibility = View.VISIBLE
        }
        description.visibility = descriptionVisibility

        stars.text = repo.stars.toString()
        forks.text = repo.forks.toString()
        try {
            if(repo.lastUpdate!=null){
                val date = repo.lastUpdate!!.replace('-','.').substringBefore('T')
                /*
                val date = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val datetime = LocalDateTime.parse(repo.lastUpdate)
                    datetime.dayOfMonth.toString() + "." + datetime.monthValue.toString() + "." + datetime.year.toString()
                } else {
                    ""
                }*/

                updated.text = "last update: " + date
            }
        }catch (ex : Exception){}

    }

    companion object {
        fun create(parent: ViewGroup): RepoViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.repo_view_item, parent, false)
            return RepoViewHolder(view)
        }
    }


}
