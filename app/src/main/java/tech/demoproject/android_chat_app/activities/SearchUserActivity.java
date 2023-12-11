package tech.demoproject.android_chat_app.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import tech.demoproject.android_chat_app.adapter.UserAdapter;
import tech.demoproject.android_chat_app.databinding.ActivitySearchUserBinding;
import tech.demoproject.android_chat_app.listeners.UserListener;
import tech.demoproject.android_chat_app.models.User;
import tech.demoproject.android_chat_app.utilities.Constants;
import tech.demoproject.android_chat_app.utilities.PreferenceManager;

public class SearchUserActivity extends AppCompatActivity implements UserListener {
    private ActivitySearchUserBinding binding;
    private PreferenceManager preferenceManager;
    private UserAdapter userAdapter;
    private List<User> users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fetchData();
        initViews();
        setActions();
    }

    private void fetchData() {
        preferenceManager = new PreferenceManager(getApplicationContext());

        users = new ArrayList<>();
        userAdapter = new UserAdapter(users,this);
        getUsers();
    }

    private void initViews() {

    }

    private void setActions() {
        binding.textCancel.setOnClickListener(v -> onBackPressed());
    }
    private void getUsers(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USER)
                .get()
                .addOnCompleteListener(task -> {
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    //-------------------------------1--------------------------------------
                    if(task.isSuccessful() && task.getResult() != null){
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
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        //---------------------------2-----------------------------
                        if(users.size() > 0){
                            binding.recycleViewContacts.setAdapter(userAdapter);
                        }

                    }
                });
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }
}