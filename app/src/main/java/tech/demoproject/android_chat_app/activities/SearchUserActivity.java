package tech.demoproject.android_chat_app.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
        setActions();
    }

    private void fetchData() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        users = new ArrayList<>();
        userAdapter = new UserAdapter(this);
        getUsers();
    }


    private void setActions() {
        binding.textCancel.setOnClickListener(v -> onBackPressed());

        //search user
        // Search user
        binding.searchView.clearFocus();
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterListContact(
                        newText,
                        users != null ? users : Collections.emptyList(),
                        userAdapter);
                return true;
            }
        });
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
                            userAdapter.setData(users);
                        }

                    }
                });
    }

    private void filterListContact(String text, List<User> users, UserAdapter adapter) {
        List<User> filterList = new ArrayList<>();
        for (User user : users) {
            if (user.name.toLowerCase(Locale.getDefault()).contains(text.toLowerCase(Locale.getDefault())) ||
                    user.email.toLowerCase(Locale.getDefault()).contains(text.toLowerCase(Locale.getDefault()))) {
                filterList.add(user);
            }
        }
        if (filterList.isEmpty()) {
            adapter.setData(Collections.emptyList());
        } else {
            adapter.setData(filterList);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }
}