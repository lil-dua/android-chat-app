package tech.demoproject.android_chat_app.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tech.demoproject.android_chat_app.databinding.ItemContainerReceivedMessageBinding;
import tech.demoproject.android_chat_app.databinding.ItemContainerSentMessageBinding;
import tech.demoproject.android_chat_app.models.ChatMessage;
import tech.demoproject.android_chat_app.utilities.EncryptionUtils;

/***
 * Created by HoangRyan aka LilDua on 9/24/2022.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatMessage> chatMessages;
    private Bitmap receiverProfileImage;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        }else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position),receiverProfileImage);
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {

        /* Because we have enable viewBinding for this project so here 'ItemContainerSentMessageBinding'
         * class is automatically generated from our layout file: 'item_container_sent_message' */
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage){
            try {
                EncryptionUtils encryptionUtils = new EncryptionUtils();
                String decryptedMessage
                        = encryptionUtils.decryptMessage(chatMessage.message, chatMessage.iv);
                binding.textMessage.setText(decryptedMessage);
                binding.textDateTime.setText(chatMessage.dateTime);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        /* Because we have enable viewBinding for this project so here 'ItemContainerReceivedMessageBinding'
         * class is automatically generated from our layout file: 'item_container_received_message' */
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding){
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage){
            try {
                EncryptionUtils encryptionUtils = new EncryptionUtils();
                String decryptedMessage
                        = encryptionUtils.decryptMessage(chatMessage.message, chatMessage.iv);
                binding.textMessage.setText(decryptedMessage);
                binding.textDateTime.setText(chatMessage.dateTime);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if(receiverProfileImage != null){
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }
}
