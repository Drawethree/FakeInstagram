package me.drawethree.fakeig.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import me.drawethree.fakeig.R;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Objects;

public class UserFeedActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private String userSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_user_feed);
        this.linearLayout = this.findViewById(R.id.linearLayout);

        Intent i = this.getIntent();

        this.userSelected = i.getStringExtra("username");

        this.setTitle(this.getResources().getString(R.string.user_feed_title, this.userSelected));

        this.loadFeed(this.userSelected);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.user_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.logout_confirm);
            builder.setIcon(R.drawable.ic_account_circle_black_24dp);
            builder.setPositiveButton(R.string.yes, (dialog, id) -> {

                ParseUser.getCurrentUser().put("online", false);
                ParseUser.getCurrentUser().saveInBackground();

                ParseUser.logOutInBackground(callback -> {
                    if (callback == null) {
                        Intent i = new Intent(this, MainActivity.class);
                        i.putExtra("logout", true);
                        this.startActivity(i);
                    }
                });
            });

            builder.setNegativeButton(R.string.no, (dialog, id) -> {

            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else if (item.getItemId() == R.id.refresh) {
            this.loadFeed(this.userSelected);
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadFeed(String userSelected) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("image");
        query.whereEqualTo("username", userSelected);
        query.orderByAscending("createdAt");

        query.findInBackground((objects, e) -> {
            if (e == null) {
                for (ParseObject obj : objects) {
                    ParseFile file = (ParseFile) obj.get("image");
                    Objects.requireNonNull(file).getDataInBackground((data, e1) -> {
                        if (e1 == null && data != null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            ImageView imageView = new ImageView(this);
                            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            imageView.setImageBitmap(bitmap);
                            linearLayout.addView(imageView);
                        }
                    });
                }
            }
        });
    }
}
