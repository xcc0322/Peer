package com.xcc0322.peer;

import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.xcc0322.peer.model.Favor;
import com.xcc0322.peer.model.User;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HomeActivity extends ListActivity {
  private static final String TAG = "HomeActivity";
  public static final int INSERT_ID = Menu.FIRST;
  private static final int DELETE_ID = Menu.FIRST + 1;
  public static final int LOGOUT_ID = Menu.FIRST + 2;

  private Dialog progressDialog;
  List<Favor> favors;

  private List<ParseObject> getFavorList() {
    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Favor.TAG);
    query.orderByDescending("_created_at");
    try {
      return query.find();
    } catch (ParseException e) {
      Log.e(TAG, e.getMessage());
    }
    return null;
  }

  protected void updateUI() {
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(HomeActivity.this,
            R.layout.todo_row);
    if (favors != null) {
        for (Favor favor : favors) {
            adapter.add((String) favor.getTitle());
        }
    }
    setListAdapter(adapter);

    progressDialog.dismiss();
    TextView empty = (TextView) findViewById(android.R.id.empty);
    empty.setVisibility(View.INVISIBLE);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.home);
    setTitle(new User().getPhoneNumber());
    registerForContextMenu(getListView());
  }

  @Override
  public void onResume() {
    super.onResume();
    TextView empty = (TextView) findViewById(android.R.id.empty);
    empty.setVisibility(View.VISIBLE);
    progressDialog = ProgressDialog.show(
        HomeActivity.this, "", getString(R.string.loading), true);
    updateFavorList();
  }

  private void updateFavorList() {
    Observable.just(getFavorList())
        .map(Favor::fromList)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            l -> {
              favors = (List) l;
              if (progressDialog.isShowing()) {
                progressDialog.dismiss();
              }
              updateUI();
            });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    boolean result = super.onCreateOptionsMenu(menu);
    menu.add(0, INSERT_ID, 0, R.string.menu_insert);
    menu.add(0, LOGOUT_ID, 0, R.string.menu_logout);
    return result;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case INSERT_ID:
        startActivity(new Intent(this, CreateFavor.class));
        return true;
      case LOGOUT_ID:
        User.logOut();
        startActivity(new Intent(this, LoginActivity.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    menu.add(0, DELETE_ID, 0, R.string.menu_delete);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case DELETE_ID:
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final Favor favor = favors.get(info.position);
        favor.deleteInBackground(new DeleteCallback() {
          public void done(ParseException e) {
            updateFavorList();
          }
        });
        return true;
    }
    return super.onContextItemSelected(item);
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    Intent i = new Intent(this, CreateFavor.class);
    i.putExtra("favorId", favors.get(position).getObjectId());
    startActivity(i);
  }
}