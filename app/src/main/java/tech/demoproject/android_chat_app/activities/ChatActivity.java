package tech.demoproject.android_chat_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import tech.demoproject.android_chat_app.R;
import tech.demoproject.android_chat_app.databinding.ActivityChatBinding;
import tech.demoproject.android_chat_app.models.User;
import tech.demoproject.android_chat_app.utilities.Constants;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
    }

    private void loadReceiverDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }
}