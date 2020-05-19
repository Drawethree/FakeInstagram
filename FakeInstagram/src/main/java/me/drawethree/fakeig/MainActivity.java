package me.drawethree.fakeig;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseAnalytics;
import com.parse.ParseUser;

import java.util.Locale;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

    // 0 - SignUp
    // 1 - LogIn
    private boolean signUpMode;

    private Button signUpBtn;
    private TextView changeModeTextView;
    private EditText passwordEditText;
    private EditText usernameEditText;
    private EditText emailEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setLanguageForApp("sk");
        this.setContentView(R.layout.activity_main);
        this.setTitle(R.string.app_name);

        this.signUpBtn = this.findViewById(R.id.signupBtn);
        this.changeModeTextView = this.findViewById(R.id.changeMode);
        this.passwordEditText = this.findViewById(R.id.passwordInput);
        this.usernameEditText = this.findViewById(R.id.usernameInput);
        this.emailEditText = this.findViewById(R.id.emailInput);
        this.changeModeTextView.setOnClickListener(this);
        this.passwordEditText.setOnKeyListener(this);

        if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getUsername() != null) {
            this.showUserlist();
        }

        ParseAnalytics.trackAppOpenedInBackground(this.getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void signUpOrLogin(View view) {

        //Sign Up
        if (!this.signUpMode) {
            if (this.usernameEditText.getText().toString().isEmpty() || this.passwordEditText.getText().toString().isEmpty()) {
                Toast.makeText(this, R.string.invalid_input, Toast.LENGTH_LONG).show();
            } else {

                ParseUser user = new ParseUser();

                user.setUsername(this.usernameEditText.getText().toString());
                user.setPassword(this.passwordEditText.getText().toString());

                if (!this.emailEditText.getText().toString().isEmpty()) {
                    user.setEmail(emailEditText.getText().toString());
                }

                user.signUpInBackground(e -> {
                    if (e == null) {
                        Toast.makeText(MainActivity.this, R.string.sign_up_success, Toast.LENGTH_LONG).show();
                        this.showUserlist();
                    } else {
                        Toast.makeText(MainActivity.this, R.string.sign_up_fail, Toast.LENGTH_LONG).show();
                        Log.i("SignUp", e.getLocalizedMessage());
                    }
                });
            }
            //Log In
        } else {
            ParseUser.logInInBackground(this.usernameEditText.getText().toString(), this.passwordEditText.getText().toString(), (user, e) -> {
                if (user != null) {
                    Toast.makeText(MainActivity.this, R.string.login_success, Toast.LENGTH_LONG).show();
                    this.showUserlist();
                } else {
                    Toast.makeText(MainActivity.this, R.string.login_fail, Toast.LENGTH_LONG).show();
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
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            this.signUpOrLogin(v);
        }
        return false;
    }

    private void showUserlist() {
        Intent i = new Intent(this, UserListActivity.class);
        this.startActivity(i);
    }

    private void setLanguageForApp(String language) {

        Locale locale;
        if (language.equals("not-set")) {
            locale = Locale.getDefault();
        } else {
            locale = new Locale(language);
        }

        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.locale = locale;
        this.getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }
}