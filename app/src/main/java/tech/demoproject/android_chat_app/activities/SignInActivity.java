package tech.demoproject.android_chat_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import tech.demoproject.android_chat_app.R;
import tech.demoproject.android_chat_app.databinding.ActivitySignInBinding;


public class SignInActivity extends AppCompatActivity {

    // ActivitySignUpBinding class is automatically generated from our layout file: 'activity_sign_in'
    private ActivitySignInBinding binding;  //View Binding : a feature that allows you to more easily write code that interface with views

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();
    }


    private void setListeners(){
        // you can see: an instance of a binding class contains direct references to all view that have an ID in the corresponding layout
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
    }
}