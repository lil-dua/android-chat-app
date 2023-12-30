package tech.demoproject.android_chat_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import tech.demoproject.android_chat_app.databinding.ActivitySignInBinding;
import tech.demoproject.android_chat_app.utilities.Constants;
import tech.demoproject.android_chat_app.utilities.PasswordHasher;
import tech.demoproject.android_chat_app.utilities.PreferenceManager;


public class SignInActivity extends AppCompatActivity {

    // ActivitySignUpBinding class is automatically generated from our layout file: 'activity_sign_in'
    private ActivitySignInBinding binding;  //View Binding : a feature that allows you to more easily write code that interface with views
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set binding
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        // if you sign in before, you won't need to sign in again
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        // set listener for all action
        setListeners();
    }



    private void setListeners(){
        // you can see: an instance of a binding class contains direct references to all view that have an ID in the corresponding layout
        binding.textRegister.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
        // set onClick for button Sign In
        binding.buttonSignIn.setOnClickListener(v -> {
            // check if all information is entered correctly
            if(isValidSignInDetails()){
                signIn();
            }
        });
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(
                        Constants.KEY_PASSWORD,
                        // check input password after hash
                        hashPassword(binding.inputPassword.getText().toString()))
                .get()
                .addOnCompleteListener(task -> {
                    //when sign in success
                    if(task.isSuccessful() && task.getResult() != null
                    && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        // Access to MainActivity
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    //when sign in failure
                    else {
                        loading(false);
                        showToast("Unable to sign in");
                    }
                });
        Log.i("HashedPassword", "signIn: " + hashPassword(binding.inputPassword.getText().toString()));

    }

    private String hashPassword(String password) {
        PasswordHasher hash = new PasswordHasher();
        try {
            return hash.hashPassword(password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Check valid sign in
    private Boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Enter valid email");
            return false;
        }else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }
        // if all information was valid and correct request.
        return true;
    }

    // loading for Sign In
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }
}