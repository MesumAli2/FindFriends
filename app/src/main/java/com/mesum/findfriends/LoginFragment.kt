package com.mesum.findfriends

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.Auth
import com.mesum.findfriends.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {
    companion object {
        const val TAG = "LoginFragment"
        const val SIGN_IN_RESULT_CODE = 1001
    }
    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!

  private val viewModel by viewModels<LoginViewModel>()
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.authButton.setOnClickListener { launchSignInFlow() }
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { state ->
            when(state){
                LoginViewModel.AuthenticationState.AUTHENTICATED-> Toast.makeText(
                    activity,
                    "Welcome You have Successfully lodged in",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun launchSignInFlow(){
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build(), SIGN_IN_RESULT_CODE
        )
    }


}