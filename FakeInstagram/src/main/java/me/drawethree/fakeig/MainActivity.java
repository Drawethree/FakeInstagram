/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package me.drawethree.fakeig;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseAnalytics;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

  // 0 - SignUp
  // 1 - LogIn
  private boolean signUpMode;
  private Button signUpBtn;
  private TextView changeTextView;
  private EditText passwordEditText;
  private EditText usernameEditText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setTitle("Fake Instagram");

    signUpBtn = findViewById(R.id.signupBtn);
    changeTextView = findViewById(R.id.changeMode);
    passwordEditText = findViewById(R.id.passwordInput);
    usernameEditText = findViewById(R.id.usernameInput);
    changeTextView.setOnClickListener(this);
    passwordEditText.setOnKeyListener(this);

    if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getUsername() != null) {
      showUserlist();
    }
    
    ParseAnalytics.trackAppOpenedInBackground(getIntent());
  }

  private void add50Points() {
    ParseQuery<ParseObject> query = ParseQuery.getQuery("Score");
    query.whereGreaterThan("score", 200);
    query.findInBackground((objects, e) -> {
      if (objects.isEmpty()) {
        return;
      }
      for (ParseObject obj : objects) {
        obj.put("score", obj.getInt("score") + 50);
        obj.saveInBackground();
      }
    });
  }

  private void queryTest1() {
    ParseQuery<ParseObject> query = ParseQuery.getQuery("Score");
    query.getInBackground("tyQ1JhwGJI", (object, e) -> {
      if (object != null && e == null) {
        Log.i("Parse Query", String.format("Object: %s %d",object.getString("username"),object.getInt("score")));
      }

    });
  }

  private void saveTest1() {
    ParseObject tweet = new ParseObject("tweet");
    tweet.put("username", "janci");
    tweet.put("tweet", "Ako sa mas?");

    tweet.saveInBackground(e -> {
      if (e==null)  {
        Log.i("Tweet", "Saved!");
      }
    });
  }

  private void signUpTest() {
    ParseUser user = new ParseUser();
    user.setPassword("test");
    user.setUsername("test");

    user.signUpInBackground(e -> {
      if (e == null) {
        Log.i("SignUp", "Success");
       } else {
        Log.i("SignUp", "Failed");
      }
    });

    user.logInInBackground("test", "test1", (user1, e) -> {
      if (user1 == null) {
        Log.i("LogIn", "Failed: " + e.toString());
      } else {
        Log.i("LogIn", "Successful");
      }
    });
  }

  public void signUp(View view) {


    //Sign Up
    if (signUpMode == false) {
      if (usernameEditText.getText().toString().isEmpty() || passwordEditText.getText().toString().isEmpty()) {
        Toast.makeText(this, "Username or password cannot be empty!", Toast.LENGTH_LONG).show();
      } else {
        ParseUser user = new ParseUser();
        user.setUsername(usernameEditText.getText().toString());
        user.setPassword(passwordEditText.getText().toString());

        user.signUpInBackground(e -> {
          if (e == null) {
            Toast.makeText(MainActivity.this, "Sign up successful!", Toast.LENGTH_LONG).show();
            showUserlist();
          } else {
            Toast.makeText(MainActivity.this, "Sign up failed!", Toast.LENGTH_LONG).show();
            Log.i("SignUp", e.toString());
          }
        });
      }
      //Log In
    } else {
      ParseUser.logInInBackground(usernameEditText.getText().toString(), passwordEditText.getText().toString(), (user, e) -> {
        if (user != null) {
          Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_LONG).show();
          showUserlist();
        } else {
          Toast.makeText(MainActivity.this, "Invalid credintials!", Toast.LENGTH_LONG).show();
        }
      });
    }
  }


  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.changeMode) {
      EditText usernameEdit = findViewById(R.id.usernameInput);
      EditText passwordEdit = findViewById(R.id.passwordInput);
      if (signUpMode == false) {
        signUpBtn.setText("Login");
        changeTextView.setText("or Sign Up");
      } else {
        signUpBtn.setText("Sign Up");
        changeTextView.setText("or Login");
      }
      usernameEdit.setText(null);
      passwordEdit.setText(null);
      signUpMode = !signUpMode;
    }
  }

  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
      signUp(v);
    }
    return false;
  }

  private void showUserlist() {
    Intent i = new Intent(this, UserListActivity.class);
    startActivity(i);
  }
}