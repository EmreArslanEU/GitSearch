package com.example.gitsearch.features.repositorydetail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navArgs
import com.example.gitsearch.R
import com.example.gitsearch.databinding.RepositoryDetailFragmentBinding
import com.example.shareddtos.Repo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RepositoryDetailFragment : Fragment() {

    companion object {
        fun newInstance() = RepositoryDetailFragment()
    }

    private val viewModel: RepositoryDetailViewModel by viewModels()
    private lateinit var binding: RepositoryDetailFragmentBinding
    private val args: RepositoryDetailFragmentArgs by navArgs()
    private lateinit var repo:LiveData<Repo>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = RepositoryDetailFragmentBinding.inflate(inflater,container, false)

        if(args.repoid == -1L)findNavController().navigateUp()
        //binding.repo = Repo(1,"","","","",0,"","","","","")
        repo = viewModel.getRepo(args.repoid)
        repo.observe(viewLifecycleOwner,{
            binding.repo = it

            binding.buttonToGithub.setOnClickListener {v ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.url))
                requireContext().startActivity(intent)
            }
        })



        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}