package tech.demoproject.android_chat_app.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tech.demoproject.android_chat_app.databinding.ItemContainerUserBinding;
import tech.demoproject.android_chat_app.listeners.UserListener;
import tech.demoproject.android_chat_app.models.User;

/***
 * Created by HoangRyan aka LilDua on 9/23/2022.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{

    private final List<User> user;
    private final UserListener userListener;

    ItemContainerUserBinding binding;

    public UserAdapter(List<User> user, UserListener userListener) {
        this.user = user;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(user.get(position));
    }

    @Override
    public int getItemCount() {
        return user.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        /* Because we have enable viewBinding for this project so here 'ItemContainerUserBinding'
        * class is automatically generated from our layout file: 'item_container_user' */
        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        // These are more than one "User" class. Make sure you select the one from our "models" package
        void setUserData(User user){
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
}
