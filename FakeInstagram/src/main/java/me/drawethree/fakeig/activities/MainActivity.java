package me.drawethree.fakeig.activities;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import me.drawethree.fakeig.R;

import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseAnalytics;
import com.parse.ParseUser;

import java.util.Locale;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

    // 0 - SignUp
    // 1 - LogIn
    private boolean signUpMode;
    private String currentLanguage;

    private Button signUpBtn;
    private TextView changeModeTextView;
    private EditText passwordEditText;
    private EditText usernameEditText;
    private EditText emailEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.createNotificationChannel();
        //this.setLanguageForApp(this.getPreferences(MODE_PRIVATE).getString("language", "sk"), false);
        this.setContentView(R.layout.activity_main);
        this.setTitle(R.string.app_name);

        this.signUpBtn = this.findViewById(R.id.signupBtn);
        this.changeModeTextView = this.findViewById(R.id.changeMode);
        this.passwordEditText = this.findViewById(R.id.passwordInput);
        this.usernameEditText = this.findViewById(R.id.usernameInput);
        this.emailEditText = this.findViewById(R.id.emailInput);

        this.changeModeTextView.setOnClickListener(this);
        this.passwordEditText.setOnKeyListener(this);


        ParseAnalytics.trackAppOpenedInBackground(this.getIntent());

        //auto-login
        if (this.getIntent().getBooleanExtra("logout", false)) {
            Snackbar.make(this.emailEditText, R.string.logout_success, Snackbar.LENGTH_LONG).show();
        } else {
            if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getUsername() != null) {

                ParseUser.getCurrentUser().put("online", true);
                ParseUser.getCurrentUser().saveInBackground();

                this.showUserlist();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null) {
            switch (item.getItemId()) {
                case R.id.menuEnglish:
                    this.setLanguageForApp("en");
                    Snackbar.make(this.usernameEditText, R.string.language_change, Snackbar.LENGTH_LONG).show();
                    break;
                case R.id.menuSlovak:
                    this.setLanguageForApp("sk");
                    Snackbar.make(this.usernameEditText, R.string.language_change, Snackbar.LENGTH_LONG).show();
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void signUpOrLogin(View view) {

        //Sign Up
        if (!this.signUpMode) {
            if (this.usernameEditText.getText().toString().isEmpty() || this.passwordEditText.getText().toString().isEmpty()) {
                Snackbar.make(view, R.string.invalid_input, Snackbar.LENGTH_LONG).show();
            } else {

                ParseUser user = new ParseUser();

                user.setUsername(this.usernameEditText.getText().toString());
                user.setPassword(this.passwordEditText.getText().toString());

                if (!this.emailEditText.getText().toString().isEmpty()) {
                    user.setEmail(emailEditText.getText().toString());
                }

                //Zaregistruj užívateľa do DB
                user.signUpInBackground(e -> {

                    if (e == null) {
                        Snackbar.make(view, R.string.sign_up_success, Snackbar.LENGTH_LONG).show();

                        user.put("online", true);
                        user.saveInBackground();

                        this.showUserlist();
                    } else {
                        Snackbar.make(view, R.string.sign_up_fail, Snackbar.LENGTH_LONG).show();
                    }
                });
            }
            //Log In
        } else {
            ParseUser.logInInBackground(this.usernameEditText.getText().toString(), this.passwordEditText.getText().toString(), (user, e) -> {

                if (user != null) {

                    ParseUser.getCurrentUser().put("online", true);
                    ParseUser.getCurrentUser().saveInBackground();

                    this.showUserlist();
                } else {
                    Snackbar.make(view, R.string.login_fail, Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.changeMode) {

            if (!this.signUpMode) {
                this.signUpBtn.setText(R.string.btn_login);
                this.changeModeTextView.setText(R.string.or_signup_txt);
                this.emailEditText.setVisibility(View.INVISIBLE);
            } else {
                this.signUpBtn.setText(R.string.btn_signup);
                this.changeModeTextView.setText(R.string.or_login_txt);
                this.emailEditText.setVisibility(View.VISIBLE);
            }

            this.usernameEditText.setText(null);
            this.passwordEditText.setText(null);
            this.emailEditText.setText(null);
            this.signUpMode = !this.signUpMode;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (v.equals(this.passwordEditText) && keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            this.hideKeyboard();
            this.signUpOrLogin(v);
        }
        return false;
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) this.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                this.getCurrentFocus().getWindowToken(), 0);
    }

    private void showUserlist() {
        Intent i = new Intent(this, UserListActivity.class);
        this.startActivity(i);
    }

    private void setLanguageForApp(String language) {

        if (this.currentLanguage != null && this.currentLanguage.equals(language)) {
            return;
        }

        this.currentLanguage = language;

        Locale locale;
        if (language.equals("not-set")) {
            locale = Locale.getDefault();
        } else {
            locale = new Locale(language);
        }

        Locale.setDefault(locale);

        Configuration config = getResources().getConfiguration();
        config.locale = locale;

        //Update strings
        this.getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        this.finish();
        startActivity(new Intent(this, MainActivity.class));

    }

    @Override
    protected void onPause() {
        SharedPreferences preferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("language", this.currentLanguage);
        super.onPause();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("FakeInstagram", "FakeInstagram", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notification channel for FakeInstagram");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}