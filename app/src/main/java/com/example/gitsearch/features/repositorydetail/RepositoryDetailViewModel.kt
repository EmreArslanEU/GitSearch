package com.example.gitsearch.features.repositorydetail

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.gitsearch.repository.GithubRepository
import com.example.shareddtos.Repo

class RepositoryDetailViewModel @ViewModelInject constructor( val repository : GithubRepository): ViewModel() {
    private var repo: LiveData<Repo> = MutableLiveData()

    fun getRepo(id:Long):LiveData<Repo>{
        if(repo.value?.id== id) return repo
        repo = repository.getRepoFromDB(id).asLiveData()
        return repo
    }

}