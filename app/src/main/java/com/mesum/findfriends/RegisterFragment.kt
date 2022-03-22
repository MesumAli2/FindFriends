package com.mesum.findfriends
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.mesum.findfriends.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar


class RegisterFragment : Fragment() {
    companion object {
        const val TAG = "LoginFragment"
        const val SIGN_IN_RESULT_CODE = 1001
    }
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebase: Firebase
    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var navController: NavController
    private lateinit var fStore : FirebaseFirestore
    private lateinit var userID : String
    private val mDb: FirebaseFirestore? = null
    private var auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fStore  = FirebaseFirestore.getInstance()

        binding.btnRegister.setOnClickListener { //launchSignInFlow()
               // registerNewEmail(binding.nameInput.text.toString(), binding.inputEmail.text.toString(), binding.inputPassword.text.toString())
            registerNewEmail1(binding.inputEmail.text.toString(),binding.inputPassword.text.toString() )
             }
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {

                LoginViewModel.AuthenticationState.AUTHENTICATED -> Toast.makeText(
                    activity,
                    "Welcome You have Successfully lodged in",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build(), SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Log.i(
                    TAG,
                    "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser}!"
                )
            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    fun registerNewEmail1(email: String, password: String?) {
        auth.createUserWithEmailAndPassword(email, password!!)
            .addOnCompleteListener (activity as MainActivity){ task ->
                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful)
                if (task.isSuccessful) {
                    Log.d(
                        TAG, "onComplete: AuthState: " + FirebaseAuth.getInstance().currentUser!!
                            .uid
                    )
                    val db = Firebase.firestore
                    //insert some default data
                    val user = User()
                    user.email = email
                    user.username = email.substring(0, email.indexOf("@"))
                    user.user_id = FirebaseAuth.getInstance().uid
                    user.password = password

                        db
                        .collection("user")
                        .document(auth.uid!!)
                        .set(user).addOnCompleteListener {
                            if (it.isSuccessful){
                                Toast.makeText(activity, "Congrats you have resgitered", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.loginFragment)

                            }
                        }
                } else {
                     Snackbar.make(view!!, "Something went wrong.", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
    }
}
