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
            //loginUser(binding.email.text.toString(), binding.password.text.toString())
            signIn(binding.email.text.toString(), binding.password.text.toString())
        }
        binding.linkRegister.setOnClickListener {
            //findNavController().navigate(R.id.closeButton)
        //    findNavController().navigate(R.id.reg)
        }
    }



  /*  private fun setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: started.")
        mAuthListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.uid)
                Toast.makeText(activity,
                    "Authenticated with: " + user.email,
                    Toast.LENGTH_SHORT
                ).show()
                val db = FirebaseFirestore.getInstance()

                val userRef = db.collection("user")
                    .document(user.uid)
                userRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "onComplete: successfully set the user client.")
                        val user: User? = User( task.result.data)
                        user.email = email
                        user.username = email.substring(0, email.indexOf("@"))
                        user.user_id = FirebaseAuth.getInstance().uid
                        user.password = password
                        (context as UserClient).user = user
                    }
                }
                val intent = Intent(activity as AppCompatActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out")
            }
            // ...
        }
    }*/


    private fun signIn() {
        //check if the fields are filled out
        if (!isEmpty(binding.email.getText().toString())
            && !isEmpty(binding.password.getText().toString())
        ) {
            Log.d(TAG, "onClick: attempting to authenticate.")
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                binding.email.getText().toString(),
                binding.password.getText().toString()
            )
                .addOnCompleteListener {  }.addOnFailureListener {
                    Toast.makeText(activity, "Authentication Failed", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(
                activity,
                "You didn't fill in all the fields.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


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


    private fun loginUser(email: String, password: String) {
        val db = Firebase.firestore
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                  if((document.data["email"] == email ) && (document.data["avatar"] == password)) {
                     // findNavController().navigate(R.id.location)
                      Log.d(TAG, "${document.data["email"]}")
                  }

                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }
}