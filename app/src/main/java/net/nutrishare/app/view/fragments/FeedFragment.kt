package net.nutrishare.app.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import net.nutrishare.app.R
import net.nutrishare.app.databinding.FragmentFeedBinding


class FeedFragment : Fragment() {

    private lateinit var binding:FragmentFeedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentFeedBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpListeners()
    }

    private fun setUpListeners(){

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_feed_fragment_to_createPostFragment)
        }

    }
}