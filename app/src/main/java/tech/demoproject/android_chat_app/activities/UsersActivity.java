package tech.demoproject.android_chat_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import tech.demoproject.android_chat_app.adapter.UserAdapter;
import tech.demoproject.android_chat_app.databinding.ActivityUsersBinding;
import tech.demoproject.android_chat_app.listeners.UserListener;
import tech.demoproject.android_chat_app.models.User;
import tech.demoproject.android_chat_app.utilities.Constants;
import tech.demoproject.android_chat_app.utilities.PreferenceManager;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();

    }
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USER)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    //-------------------------------1--------------------------------------
                    if(task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        //---------------------------2-----------------------------
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.publicKey = queryDocumentSnapshot.getString(Constants.KEY_RECEIVER_PUBLIC_KEY);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        //---------------------------2-----------------------------
                        if(users.size() > 0){
                            UserAdapter userAdapter = new UserAdapter(this);
                            userAdapter.setData(users);
                            binding.usersRecycleView.setAdapter(userAdapter);
                            binding.usersRecycleView.setVisibility(View.VISIBLE);
                        }else{
                            showErrorMessage();
                        }

                    }else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s","No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    // set Listener for each user on user list - access to ChatActivity
    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}