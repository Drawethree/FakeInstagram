package me.drawethree.fakeig.activities;

import android.app.PendingIntent;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import me.drawethree.fakeig.R;
import me.drawethree.fakeig.adapters.UserListAdapter;

/**
 * Aktivita na zobrazenie vsetkych uzivatelov a ich status.
 */
public class UserListActivity extends AppCompatActivity {

    private static final int UPLOAD_REQUEST_CODE = 4562;

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

        if (this.getIntent().getBooleanExtra("login", false)) {

            ParseUser.getCurrentUser().put("online", true);
            ParseUser.getCurrentUser().saveInBackground();

            this.getIntent().removeExtra("login");
            Snackbar.make(this.userListView, R.string.login_success, Snackbar.LENGTH_LONG).show();
        }

        this.refreshUsers();
    }

    /**
     * Metoda na ziskanie vsetkych uzivatelov z databazy a na vyplnenie ListViewu tymito uzivatelmi.
     */
    private void refreshUsers() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
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
    }

    /**
     * Metoda na vyplnenie menu cez MenuInflater.
     * @param menu - Menu na vyplnenie
     * @return true - zobrazi menu, false - nezobrazi
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.user_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Metoda na spracovanie kliknutia polozky v menu aktivity. V tomto pripade buď odhlasi aktualneho pouzivatela, alebo znovu nacita zoznam vsetkych uzivatelov.
     * @param item - Polozka menu, ktora bola zvolena
     * @return Return false to allow normal menu processing to
     *         proceed, true to consume it here.
     */
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
            this.refreshUsers();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Metoda na spustenie Intentu ktory si vypyta od uzivatela vyber hociakej fotky.
     */
    private void requestPhoto() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        this.startActivityForResult(i, UPLOAD_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UPLOAD_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            Uri selectedImage = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream);

                ParseFile file = new ParseFile(UUID.randomUUID().toString() + ".png", outputStream.toByteArray());

                ParseObject object = new ParseObject("image");
                object.put("image", file);
                object.put("username", ParseUser.getCurrentUser().getUsername());

                //Snackbar.make(userListView, R.string.img_upload_progress, Snackbar.LENGTH_INDEFINITE).show();
                object.saveInBackground(e -> {
                    if (e == null) {
                        Snackbar.make(userListView, R.string.img_upload_success, Snackbar.LENGTH_LONG).show();
                        this.sendUploadNotification(ParseUser.getCurrentUser());
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

    /**
     * Metoda na poslanie notifikacie po uspesnom nahrati obrazku.
     * cerpané z https://developer.android.com/training/notify-user/build-notification
     * @param user - Uzivatel ktory nahral fotku
     */
    private void sendUploadNotification(ParseUser user) {
        Intent intent = new Intent(this, UserFeedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("username", user.getUsername());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "FakeInstagram")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.notification_user_upload, user.getUsername()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(5235234, builder.build());
    }
}
