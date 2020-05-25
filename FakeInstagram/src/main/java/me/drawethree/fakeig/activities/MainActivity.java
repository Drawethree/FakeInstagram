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


/**
 * Hlavna aktivita aplikacie. Umoznuje prihlasit sa / zaregistrovat sa alebo zmenit jazyk aplikacie.
 */
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
        ParseAnalytics.trackAppOpenedInBackground(this.getIntent());
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


        //auto-login
        if (this.getIntent().getBooleanExtra("logout", false)) {
            Snackbar.make(this.emailEditText, R.string.logout_success, Snackbar.LENGTH_LONG).show();
        } else {
            if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getUsername() != null) {
                this.showUserlist();
            }
        }

    }

    /**
     * Metoda na spracovanie kliknutia polozky v menu aktivity. V tomto pripade nastavi jazyk aplikacie.
     *
     * @param item - Polozka menu, ktora bola zvolena
     * @return Return false to allow normal menu processing to
     * proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuEnglish:
                this.setLanguageForApp("en");
                break;
            case R.id.menuSlovak:
                this.setLanguageForApp("sk");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Metoda na vyplnenie menu cez MenuInflater.
     *
     * @param menu - Menu na vyplnenie
     * @return true - zobrazi menu, false - nezobrazi
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Metoda na prihlasenie / registraciu uživatela.
     * @param view - View ktory spustil túto akciu
     */
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

                //Zaregistruj uživatela do DB
                user.signUpInBackground(e -> {

                    if (e == null) {
                        Snackbar.make(view, R.string.sign_up_success, Snackbar.LENGTH_LONG).show();

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
                    this.showUserlist();
                } else {
                    Snackbar.make(view, R.string.login_fail, Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }


    /**
     * Metoda na zmenenie modu prihlasenia / registracie
     * @param v - View ktory bol kliknuty
     */
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

    /**
     *  Metoda na spracovanie stlacenia klavesy na kvavesnici
     * @param view - View v ktorom bolo tlacitko na klavesnici zmacknute
     * @param keyCode - ciselny kod zmacknuteho tlacitka
     * @param event - Akcia pri stlaceni
     * @return
     */
    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if (view.equals(this.passwordEditText) && keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            this.hideKeyboard();
            this.signUpOrLogin(view);
        }
        return false;
    }

    /**
     * Metoda na skrytie klavesnice
     */
    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) this.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                this.getCurrentFocus().getWindowToken(), 0);
    }

    /**
     * Metoda na spustenie aktivity UserListActivity.
     */
    private void showUserlist() {
        Intent i = new Intent(this, UserListActivity.class);
        i.putExtra("login", true);
        this.startActivity(i);
    }

    /**
     * Metoda na nastavenie jazyka aplikacie.
     *
     * @param language en,sk,not-set
     */
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
        Snackbar.make(this.usernameEditText, R.string.language_change, Snackbar.LENGTH_LONG).show();

    }

    @Override
    protected void onPause() {
        SharedPreferences preferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("language", this.currentLanguage);
        editor.putBoolean("signUpMode", this.signUpMode);
        editor.commit();
        super.onPause();
    }

    /**
     * Metoda na vytvorenie notifikacneho kanala pre android API 26 a vyssie.
     * cerpane z https://developer.android.com/training/notify-user/build-notification
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("FakeInstagram", "FakeInstagram", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notification channel for FakeInstagram");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}