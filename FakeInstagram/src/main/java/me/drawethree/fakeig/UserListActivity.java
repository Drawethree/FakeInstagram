package me.drawethree.fakeig;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class UserListActivity extends AppCompatActivity {

    private static final int SHARE_REQUEST_CODE = 485447;


    private ListView userListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_user_list);
        this.setTitle(R.string.users);
        this.userListView = this.findViewById(R.id.userListView);

        ArrayList<String> names = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_selectable_list_item, names);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.addAscendingOrder("username");

        query.findInBackground((objects, e) -> {
            if (e == null && !objects.isEmpty()) {
                for (ParseUser u : objects) {
                    names.add(u.getUsername());
                }
                UserListActivity.this.userListView.setAdapter(adapter);
            }
        });

        this.userListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(UserListActivity.this, UserFeedActivity.class);
            i.putExtra("username", names.get(position));
            this.startActivity(i);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.share_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share) {
            this.requestPhoto();
        } else if (item.getItemId() == R.id.logout) {
            ParseUser.logOut();
            Intent i = new Intent(this, MainActivity.class);
            this.startActivity(i);
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
                        Toast.makeText(UserListActivity.this, R.string.img_upload_success, Toast.LENGTH_LONG).show();
                    } else {
                        Log.i("ImgUpload", e.getLocalizedMessage());
                        Toast.makeText(UserListActivity.this, R.string.img_upload_failed, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
