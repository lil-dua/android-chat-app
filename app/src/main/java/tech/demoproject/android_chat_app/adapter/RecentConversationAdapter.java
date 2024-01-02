package tech.demoproject.android_chat_app.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tech.demoproject.android_chat_app.databinding.ItemContainerRecentConversionBinding;
import tech.demoproject.android_chat_app.listeners.ConversionListener;
import tech.demoproject.android_chat_app.models.ChatMessage;
import tech.demoproject.android_chat_app.models.User;

/***
 * Created by HoangRyan aka LilDua on 9/25/2022.
 */
public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversionViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;

    public RecentConversationAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder {

       ItemContainerRecentConversionBinding binding;

       ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding){
           super(itemContainerRecentConversionBinding.getRoot());
           binding = itemContainerRecentConversionBinding;
       }

       void setData(ChatMessage chatMessage){
           binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage));
           binding.textName.setText(chatMessage.conversionName);
           binding.textRecentMessage.setText(chatMessage.message);
           binding.textDateTime.setText(getTimeAgo(chatMessage.dateObject.toString()));
           binding.getRoot().setOnClickListener(v -> {
               User user = new User();
               user.id = chatMessage.conversionId;
               user.name = chatMessage.conversionName;
               user.image = chatMessage.conversionImage;
               conversionListener.onConversionClicked(user);
           });
       }

    }

    public static String getTimeAgo(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

        try {
            Date date = dateFormat.parse(dateString);
            long timeInMillis = date.getTime();
            long currentTimeMillis = System.currentTimeMillis();

            return DateUtils.getRelativeTimeSpanString(timeInMillis, currentTimeMillis, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
}
