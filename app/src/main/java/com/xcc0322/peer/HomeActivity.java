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

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HomeActivity extends ListActivity {
  private static final String TAG = "HomeActivity";
  private static final int ACTIVITY_CREATE = 0;
  private static final int ACTIVITY_EDIT = 1;

  public static final int INSERT_ID = Menu.FIRST;
  private static final int DELETE_ID = Menu.FIRST + 1;

  private Dialog progressDialog;
  List<ParseObject> todos;

  private Void delete(ParseObject todo) {
    try {
      todo.delete();
    } catch (ParseException e) {
      Log.e(TAG, e.getMessage());
    }
    return null;
  }

  private Void getTodoList() {
    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Todo");
    query.orderByDescending("_created_at");
    try {
        todos = query.find();
    } catch (ParseException e) {
        Log.e(TAG, e.getMessage());
    }
    return null;
  }

  protected void updateUI() {
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(HomeActivity.this,
            R.layout.todo_row);
    if (todos != null) {
        for (ParseObject todo : todos) {
            adapter.add((String) todo.get("name"));
        }
    }
    setListAdapter(adapter);

    progressDialog.dismiss();
    TextView empty = (TextView) findViewById(android.R.id.empty);
    empty.setVisibility(View.VISIBLE);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.home);
    registerForContextMenu(getListView());
  }

  @Override
  public void onResume() {
    super.onResume();
    updateToDoList();
  }

  private void updateToDoList() {
    TextView empty = (TextView) findViewById(android.R.id.empty);
    empty.setVisibility(View.INVISIBLE);
    progressDialog = ProgressDialog.show(
        HomeActivity.this, "", getString(R.string.loading), true);

    Observable.just(getTodoList())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            r -> {
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
    return result;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case INSERT_ID:
        startActivity(new Intent(this, CreateToDo.class));
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
            final ParseObject todo = todos.get(info.position);
            Observable.just(delete(todo))
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(r -> {updateToDoList();});
            return true;
    }
    return super.onContextItemSelected(item);
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    Intent i = new Intent(this, CreateToDo.class);
    i.putExtra("todoId", todos.get(position).getObjectId());
    startActivity(i);
  }
}