package me.drawethree.fakeig.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.List;

import me.drawethree.fakeig.R;

public class UserListAdapter extends ArrayAdapter<ParseUser> {

    public UserListAdapter(Context context, List<ParseUser> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ParseUser user = this.getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_list_item, parent, false);
        }

        TextView tvUsername = convertView.findViewById(R.id.userName);
        tvUsername.setText(user.getUsername());

        TextView tvOnline = convertView.findViewById(R.id.online);
        tvOnline.setText(user.getBoolean("online") ? getContext().getResources().getString(R.string.status_online) : getContext().getResources().getString(R.string.status_offline));
        return convertView;
    }
}
