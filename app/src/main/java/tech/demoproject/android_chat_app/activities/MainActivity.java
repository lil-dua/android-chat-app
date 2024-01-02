package tech.demoproject.android_chat_app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import tech.demoproject.android_chat_app.R;
import tech.demoproject.android_chat_app.adapter.RecentConversationAdapter;
import tech.demoproject.android_chat_app.adapter.UserAdapter;
import tech.demoproject.android_chat_app.databinding.ActivityMainBinding;
import tech.demoproject.android_chat_app.listeners.ConversionListener;
import tech.demoproject.android_chat_app.listeners.UserListener;
import tech.demoproject.android_chat_app.models.ChatMessage;
import tech.demoproject.android_chat_app.models.User;
import tech.demoproject.android_chat_app.utilities.Constants;
import tech.demoproject.android_chat_app.utilities.PreferenceManager;


public class MainActivity extends BaseActivity implements ConversionListener, UserListener {

    // ActivitySignUpBinding class is automatically generated from our layout file: 'activity_sign_in'
    private ActivityMainBinding binding;  //View Binding : a feature that allows you to more easily write code that interface with views
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private List<User> users;
    private RecentConversationAdapter conversationAdapter;
    private UserAdapter userAdapter;
    private FirebaseFirestore database;
    private Integer keyType = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        ini();
        loadUserDetails(); //load user information
        keyType = Constants.VIEW_TYPE_CONVERSATIONS;
        getToken();
        setListeners();
        listenConversations();

    }

    private void ini(){
        conversations = new ArrayList<>();
        users = new ArrayList<>();
        database = FirebaseFirestore.getInstance();
        conversationAdapter = new RecentConversationAdapter(conversations,this);
        binding.conversationRecycleView.setAdapter(conversationAdapter);
        userAdapter = new UserAdapter(this);
    }

    private void setListeners(){
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),UsersActivity.class)));
        binding.textConversations.setOnClickListener(v -> onRecentConversationClick());
        binding.textFriends.setOnClickListener(v -> onFriendClick());
        binding.imageSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchUserActivity.class)));
    }

    private void onRecentConversationClick() {
        keyType = Constants.VIEW_TYPE_CONVERSATIONS;
        onSelectedEvent();
    }

    private void onFriendClick() {
        keyType = Constants.VIEW_TYPE_FRIENDS;
        onSelectedEvent();
    }

    private void onSelectedEvent() {
        TransitionManager.beginDelayedTransition(binding.constraintMain, new AutoTransition());
        if(Objects.equals(keyType, Constants.VIEW_TYPE_CONVERSATIONS)) {
            //set title
            binding.textTitle.setText(R.string.recent_conversations);
            //set color
            binding.textConversations.setBackgroundResource(R.drawable.background_primary);
            binding.textConversations.setTextColor(ContextCompat.getColor(this,R.color.white));
            binding.textFriends.setBackgroundResource(R.drawable.background_transparent);
            binding.textFriends.setTextColor(ContextCompat.getColor(this,R.color.secondary_text));

            //init view
            binding.conversationRecycleView.setAdapter(conversationAdapter);

        }else {
            //set title
            binding.textTitle.setText(R.string.all_friends);
            //set color
            binding.textFriends.setBackgroundResource(R.drawable.background_primary);
            binding.textFriends.setTextColor(ContextCompat.getColor(this,R.color.white));
            binding.textConversations.setBackgroundResource(R.drawable.background_transparent);
            binding.textConversations.setTextColor(ContextCompat.getColor(this,R.color.secondary_text));

            //init view
            getUsers();
        }
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

    private void listenConversations(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVED_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
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
                            user.publicKey = queryDocumentSnapshot.getString(Constants.KEY_RECEIVER_PUBLIC_KEY);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        //---------------------------2-----------------------------
                        if(users.size() > 0){
                            binding.conversationRecycleView.setAdapter(userAdapter);
                            userAdapter.setData(users);
                        }

                    }
                });
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error ) -> {
        if(error != null){
            return;
        }
        if(value != null) {
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVED_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVED_ID);
                        chatMessage.receiverPublicKey = documentChange.getDocument().getString(Constants.KEY_RECEIVER_PUBLIC_KEY);
                    }else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.senderPublicKey = documentChange.getDocument().getString(Constants.KEY_SENDER_PUBLIC_KEY);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.iv = documentChange.getDocument().getString(Constants.KEY_IV);
                    conversations.add(chatMessage);
                }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for(int i = 0; i < conversations.size(); i++){
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVED_ID);
                        if(conversations.get(i).senderId.equals(senderId) && conversations.get(i).equals(receiverId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            //After the loop
            Collections.sort(conversations, (obj1,obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationAdapter.notifyDataSetChanged();
            binding.conversationRecycleView.smoothScrollToPosition(0);
            binding.conversationRecycleView.setVisibility(View.VISIBLE);
        }
    };

    /** In the dependencies (in the app-level build.gradle)
     * if you are using: implementation 'com.google.firebase:firebase-messaging:20.1.0'
     * you should change to: 'com.google.firebase:firebase-messaging:23.0.5'
     * because getToken() method after getInstance() will not exist.
     * This was the error message supplied by android studio*/
    private void getToken(){
       FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token);
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

    // set onClick for recent conversion on screen
    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }
}