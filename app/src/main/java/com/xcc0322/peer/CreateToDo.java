package com.xcc0322.peer;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CreateToDo extends Activity {
  private static final String TAG = "CreateToDo";
  @InjectView(R.id.name) EditText nameText;
  ParseObject todo;

  private void getTodoById(String objectId) {
    ParseQuery<ParseObject> query = ParseQuery.getQuery("Todo");
    query.getInBackground(objectId, new GetCallback<ParseObject>() {
      public void done(ParseObject object, ParseException e) {
        if (null == e) {
          CreateToDo.this.todo = object;
          String name = (String) todo.get("name");
          nameText.setText(name);
        } else {
          Log.e(TAG, e.getMessage());
        }
      }
    });
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.create_to_do);
    setTitle(R.string.create_todo);
    ButterKnife.inject(this);

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      String objectId = extras.getString("todoId");
      getTodoById(objectId);
    }
  }

  @OnClick(R.id.confirm)
  void onSaveClicked() {
    String name = nameText.getText().toString();
    if (null == todo) {
      todo = new ParseObject("Todo");
    }
    todo.put("name", name);
    todo.saveInBackground(new SaveCallback() {
      public void done(ParseException e) {
        finish();
      }
    });
  }
}
