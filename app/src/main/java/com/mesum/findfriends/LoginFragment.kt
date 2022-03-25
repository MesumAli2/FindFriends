package com.mesum.findfriends

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mesum.findfriends.RegisterFragment.Companion.TAG
import com.mesum.findfriends.databinding.FragmentLoginBinding
import android.text.TextUtils.isEmpty
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener





class LoginFragment : Fragment() {

    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var mAuthListener: AuthStateListener? = null
    var auth = FirebaseAuth.getInstance()


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
        binding.emailSignInButton.setOnClickListener {
            signIn(binding.email.text.toString(), binding.password.text.toString())
        }
        binding.linkRegister.setOnClickListener {
          findNavController().navigate(R.id.registerFragment)
        }
    }




    //Signe in the user using fireBase Authentication
    private fun signIn(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity as MainActivity){task ->
                    if (task.isSuccessful){
                        Toast.makeText(activity, "Congrats that's a success", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.location)
                    }else{
                        Toast.makeText(activity, "Unsucessful", Toast.LENGTH_SHORT).show()
                    }

            }
    }



}