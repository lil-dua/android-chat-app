package tech.demoproject.android_chat_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;


import java.util.HashMap;

import tech.demoproject.android_chat_app.databinding.ActivityMainBinding;
import tech.demoproject.android_chat_app.utilities.Constants;
import tech.demoproject.android_chat_app.utilities.PreferenceManager;


public class MainActivity extends AppCompatActivity {

    // ActivitySignUpBinding class is automatically generated from our layout file: 'activity_sign_in'
    private ActivityMainBinding binding;  //View Binding : a feature that allows you to more easily write code that interface with views
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails(); //load user information
        getToken();
        setListeners();

    }

    private void setListeners(){
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),UsersActivity.class)));
    }

    // load user information
    private void loadUserDetails(){
        // load name
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        // load image profile
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /** In the dependencies (in the app-level build.gradle)
     * if you are using: implementation 'com.google.firebase:firebase-messaging:20.1.0'
     * you should change to: 'com.google.firebase:firebase-messaging:23.0.5'
     * because getToken() method after getInstance() will not exist.
     * This was the error message supplied by android studio*/
    private void getToken(){
       FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USER)
                                                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    // sign out ----------------------------------------------------
    private void signOut(){
        showToast("Sign out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USER).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        // When you sign out, the token will be remove from Firebase Database
        HashMap<String,Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Unable to sign out"));
    }
}