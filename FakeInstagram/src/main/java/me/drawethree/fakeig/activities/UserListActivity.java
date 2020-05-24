package me.drawethree.fakeig.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import me.drawethree.fakeig.R;
import me.drawethree.fakeig.adapters.UserListAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class UserListActivity extends AppCompatActivity {

    private static final int SHARE_REQUEST_CODE = 1;


    private ListView userListView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_user_list);
        this.setTitle(R.string.users);
        this.userListView = this.findViewById(R.id.userListView);
        this.fab = this.findViewById(R.id.fab);

        this.fab.setOnClickListener(view -> this.requestPhoto());

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.addAscendingOrder("username");

        query.findInBackground((objects, e) -> {
            if (e == null && !objects.isEmpty()) {
                UserListActivity.this.userListView.setAdapter(new UserListAdapter(this, objects));
            }
        });

        this.userListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(UserListActivity.this, UserFeedActivity.class);
            i.putExtra("username", ((TextView) view.findViewById(R.id.userName)).getText());
            this.startActivity(i);
        });

        Snackbar.make(this.userListView, R.string.login_success, Snackbar.LENGTH_LONG).show();
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

                ParseUser.logOut();
                Snackbar.make(userListView, R.string.logout_success, Snackbar.LENGTH_LONG).show();
                Intent i = new Intent(UserListActivity.this, MainActivity.class);
                UserListActivity.this.startActivity(i);
            });

            builder.setNegativeButton(R.string.no, (dialog, id) -> {

            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestPhoto() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        this.startActivityForResult(i, SHARE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SHARE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            Uri selectedImage = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                ParseFile file = new ParseFile(UUID.randomUUID().toString() + ".png", outputStream.toByteArray());

                ParseObject object = new ParseObject("image");
                object.put("image", file);
                object.put("username", ParseUser.getCurrentUser().getUsername());

                object.saveInBackground(e -> {
                    if (e == null) {
                        Snackbar.make(userListView, R.string.img_upload_success, Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(userListView, R.string.img_upload_failed, Snackbar.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
