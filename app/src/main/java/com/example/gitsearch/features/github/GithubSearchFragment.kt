package com.example.gitsearch.features.github

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gitsearch.R
import com.example.gitsearch.adapter.ReposAdapter
import com.example.gitsearch.databinding.GithubSearchFragmentBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.util.*

@AndroidEntryPoint
class GithubSearchFragment : Fragment() {

    companion object {
        fun newInstance() = GithubSearchFragment()
    }

    private val viewModel: GithubSearchViewModel by viewModels()
    private lateinit var binding: GithubSearchFragmentBinding
    private val adapter = ReposAdapter()

    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        search("android")
        binding = GithubSearchFragmentBinding.inflate(inflater,container,false)
        initSearch("android")
        //adapter.refresh()

        binding.progressBarRepoList.isIndeterminate=true


        adapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading){
                binding.progressBarRepoList.show()
                //binding.progressBarRepoList.startAnimation(binding.progressBarRepoList.animation)
            }
            else{


                if(loadState.append is LoadState.Loading){
                    binding.progressBarRepoList.show()
                }
                else binding.progressBarRepoList.hide()
                // getting the error
                val error = when {
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                error?.let {
                    Toast.makeText(context, it.error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.reposwiperefresh.setOnRefreshListener {
            if(binding.reposwiperefresh.isRefreshing)
            adapter.refresh()
            binding.reposwiperefresh.isRefreshing=false
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        //search("android")
        binding.repoRecyclerview.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    private fun initSearch(query: String) {
        binding.searchRepo.setText(query)

        binding.searchRepo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updateRepoListFromInput()
                true
            } else {
                false
            }
        }

        binding.searchRepo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            private var timer = Timer();
            private val DELAY: Long = 500; // milliseconds

            override fun afterTextChanged(p0: Editable?) {
                timer.cancel();
                timer = Timer();
                timer.schedule(
                    object : TimerTask() {
                        override fun run() {

                            updateRepoListFromInput()
                        }

                    }, DELAY)
            }

        })

        // Scroll to top when the list is refreshed from network.
        lifecycleScope.launch {
            adapter.loadStateFlow
                // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy { it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .filter { it.refresh is LoadState.NotLoading }
                .collect {binding.repoRecyclerview.scrollToPosition(0) }
        }
    }

    private fun updateRepoListFromInput() {
        binding.searchRepo.text?.trim().let {
            if (it!!.isNotEmpty()) {
                search(it.toString())
            }
        }
    }

    private fun search(query: String) {
        // Make sure we cancel the previous job before creating a new one
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            viewModel.searchRepo(query).collectLatest {
                adapter.submitData(it)
            }
        }
    }

}