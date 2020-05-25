package me.drawethree.fakeig.parse;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

import me.drawethree.fakeig.R;


/**
 * Aktivita na spojazdnenie Parse databazy.
 */
public class StarterApplication extends Application {

  /**
   *  Metoda onCreate nadviaze konekciu s Parse databazou.
   *  Cerpane z https://docs.parseplatform.org/android/guide/
   */
  @Override
  public void onCreate() {
    super.onCreate();

    Parse.enableLocalDatastore(this);

    Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
            .applicationId(this.getResources().getString(R.string.parse_app_id))
            .clientKey(this.getResources().getString(R.string.parse_client_key))
            .server(this.getResources().getString(R.string.parse_server_link))
            .build()
    );


    ParseUser.enableAutomaticUser();

    ParseACL defaultACL = new ParseACL();
    defaultACL.setPublicReadAccess(true);
    defaultACL.setPublicWriteAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);

  }
}
