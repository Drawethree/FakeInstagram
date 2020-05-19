/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package me.drawethree.fakeig.parse;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

import me.drawethree.fakeig.R;


public class StarterApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);

    //Initialize of Parse server
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
