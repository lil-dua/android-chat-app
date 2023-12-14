package tech.demoproject.android_chat_app.adapter;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

import tech.demoproject.android_chat_app.models.User;

/***
 * Created by HoangRyan aka LilDua on 12/14/2023.
 */
public class UserDiffCallback  extends DiffUtil.Callback {

    private final List<User> oldList;
    private final List<User> newList;

    public UserDiffCallback(List<User> oldList, List<User> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0 ;
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).id.equals(newList.get(newItemPosition).id);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}