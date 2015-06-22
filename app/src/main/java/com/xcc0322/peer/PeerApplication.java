package com.xcc0322.peer;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;

import android.app.Application;

public class PeerApplication extends Application {
  public static final String PARSE_APPLICATION_ID = BuildConfig.PARSE_APPLICATION_ID;
  public static final String PARSE_CLIENT_KEY = BuildConfig.PARSE_CLIENT_KEY;

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
    Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);

    ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.

    defaultACL.setPublicReadAccess(true);
    // ParseACL.setDefaultACL(defaultACL, true);
  }
}
