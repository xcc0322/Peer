package com.xcc0322.peer;

import com.baidu.location.LocationClient;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseUser;

import android.app.Application;

public class PeerApplication extends Application {
  private static final String APPLICATION_ID =
      "hFyJ3QxrARb3j64sXwXwp49s5QFolaYJCmtPZaGU";
  private static final String CLIENT_KEY =
      "K4qm3Xnahm6WvMNToVJRJLr346rPVHnmTK4ktyKB";

  @Override
  public void onCreate() {
    super.onCreate();
    initParse();
  }

  private void initParse() {

    // Initialize Crash Reporting.
    ParseCrashReporting.enable(this);

    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);

    // Add your initialization code here
    Parse.initialize(this, APPLICATION_ID, CLIENT_KEY);

    ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.
    defaultACL.setPublicReadAccess(true);
    // ParseACL.setDefaultACL(defaultACL, true);
  }
}
