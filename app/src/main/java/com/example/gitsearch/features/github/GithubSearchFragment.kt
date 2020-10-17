package com.example.gitsearch.features.github

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.gitsearch.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GithubSearchFragment : Fragment() {

    companion object {
        fun newInstance() = GithubSearchFragment()
    }

    private val viewModel: GithubSearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.github_search_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}