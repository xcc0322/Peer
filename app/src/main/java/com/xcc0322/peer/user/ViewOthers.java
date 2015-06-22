package com.xcc0322.peer.user;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseUser;
import com.xcc0322.peer.R;
import com.xcc0322.peer.model.User;

import java.text.DateFormat;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ViewOthers extends ActionBarActivity {
  @InjectView(R.id.photo) ParseImageView photoView;
  @InjectView(R.id.name) TextView nameView;
  @InjectView(R.id.bio) TextView bioView;
  @InjectView(R.id.birthday) TextView birthdayView;

  User user;
  private Dialog progressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.view_others);
    ButterKnife.inject(this);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      String userId = extras.getString("userId");
      progressDialog = ProgressDialog.show(
          this, "", getString(R.string.loading), true);
      user = new User(userId, new GetCallback<ParseUser>() {
        public void done(ParseUser object, ParseException e) {
          progressDialog.dismiss();
          updateUserUI(user);
        }
      });
    } else {
      user = new User();
      updateUserUI(user);
    }
  }

  private void updateUserUI(User user) {
    if (null == user) {
      return;
    }
    nameView.setText(user.getName());
    bioView.setText(user.getBio());
    if (null != user.getBirthday()) {
      String birthdayString = DateFormat.getDateInstance().format(
          user.getBirthday());
      birthdayView.setText(birthdayString);
    }
    user.fillPhotoView(this, photoView);
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @OnClick(R.id.phone)
  public void onClickPhone() {
    String number = user.getPhoneNumber();
    Intent intent = new Intent(Intent.ACTION_DIAL);
    intent.setData(Uri.parse("tel:" + number));
    startActivity(intent);
  }
}
