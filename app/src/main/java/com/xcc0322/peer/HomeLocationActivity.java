package com.xcc0322.peer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.xcc0322.peer.favor.EditFavor;
import com.xcc0322.peer.favor.FavorActivity;
import com.xcc0322.peer.favor.ViewFavor;
import com.xcc0322.peer.model.User;
import com.xcc0322.peer.user.ViewUser;
import com.xcc0322.peer.util.LocationUtil;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class HomeLocationActivity extends ActionBarActivity implements
    OnGetGeoCoderResultListener {
  double latitude = 31.304612;
  double longitude = 121.509937;

  GeoCoder mSearch;
  BaiduMap mBaiduMap;
  MapView mMapView;

  LatLng userLocation;

  @InjectView(R.id.address)
  EditText addressText;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SDKInitializer.initialize(this.getApplicationContext());
    setContentView(R.layout.activity_home);
    ButterKnife.inject(this);

    if (!User.isLoggedIn()) {
      startActivity(new Intent(this, LoginActivity.class));
      finish();
    }

    mMapView = (MapView) findViewById(R.id.bmapView);
    mBaiduMap = mMapView.getMap();

    mSearch = GeoCoder.newInstance();
    mSearch.setOnGetGeoCodeResultListener(this);

    Bundle bundle = getIntent().getExtras();
    if (null != bundle) {
      latitude = bundle.getDouble(EditFavor.KEY_LATITUDE);
      longitude = bundle.getDouble(EditFavor.KEY_LONGITUDE);
      getReverseGeoCode();
    } else {
      User user = new User();
      if (null != user.getLocation()) {
        latitude = user.getLocation().getLatitude();
        longitude = user.getLocation().getLongitude();
        getReverseGeoCode();
      } else {
        LocationUtil.getCurrentLocation(this, new MyLocationListener());
      }
    }
  }

  public class MyLocationListener implements BDLocationListener {
    @Override
    public void onReceiveLocation(BDLocation location) {
      latitude = location.getLatitude();
      longitude = location.getLongitude();
      LocationUtil.getAddress(HomeLocationActivity.this, latitude, longitude,
          HomeLocationActivity.this);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_home_location, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_list:
        startActivity(new Intent(this, FavorActivity.class));
        return true;
      case R.id.action_insert:
        startActivity(new Intent(this, EditFavor.class));
        return true;
      case R.id.action_logout:
        User.logOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
        return true;
      case R.id.action_me:
        startActivity(new Intent(this, ViewUser.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public void getReverseGeoCode() {
    LatLng ptCenter = new LatLng(latitude, longitude);
    mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ptCenter));
  }

  @OnClick(R.id.geocode)
  public void getGeoCode() {
    mSearch.geocode(new GeoCodeOption().city("").address(
        addressText.getText().toString()));
  }

  @Override
  protected void onPause() {
    mMapView.onPause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    mMapView.onResume();
    super.onResume();
  }

  @Override
  protected void onDestroy() {
    mMapView.onDestroy();
    mSearch.destroy();
    super.onDestroy();
  }

  @Override
  public void onGetGeoCodeResult(GeoCodeResult result) {
    if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
      Toast.makeText(HomeLocationActivity.this, R.string.location_no_result,
          Toast.LENGTH_LONG).show();
      return;
    }
    userLocation = result.getLocation();
    updateMapLocation(userLocation);
    Toast.makeText(HomeLocationActivity.this, R.string.location_success,
        Toast.LENGTH_LONG).show();
  }

  @Override
  public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
    if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
      Toast.makeText(HomeLocationActivity.this, R.string.location_no_result,
          Toast.LENGTH_LONG).show();
      return;
    }
    userLocation = result.getLocation();
    updateMapLocation(userLocation);
    addressText.setText(result.getAddress());

    Toast.makeText(HomeLocationActivity.this, result.getAddress(),
        Toast.LENGTH_LONG).show();
  }

  private void updateMapLocation(LatLng location) {
    mBaiduMap.clear();
    queryNearBy(location);
    mBaiduMap.addOverlay(new MarkerOptions().position(location)
        .icon(BitmapDescriptorFactory
            .fromResource(R.drawable.ic_map_point_marker)));
    mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(location, 15f));

    mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
       @Override
       public boolean onMarkerClick(Marker marker) {
         switchToViewFavor(marker.getPosition());
         return false;
       }
     });
    latitude = location.latitude;
    longitude = location.longitude;
  }

  private void updateFavorLocation(double latitude, double longitude) {
    LatLng location = new LatLng(latitude, longitude);
    mBaiduMap.addOverlay(new MarkerOptions().position(location)
        .icon(BitmapDescriptorFactory
            .fromResource(R.drawable.ic_map_marker_sun)));
  }

  private void queryNearBy(LatLng location) {
    final ParseGeoPoint userLocation = new ParseGeoPoint(location.latitude, location.longitude);
    ParseQuery<ParseObject> query = ParseQuery.getQuery("Favor");
    query.whereNear("destination", userLocation);
    query.setLimit(20);
    query.findInBackground(new FindCallback<ParseObject>() {
      @Override
      protected void finalize() throws Throwable {
        super.finalize();
      }

      @Override
      public void done(List<ParseObject> favors, ParseException e) {
        for(ParseObject favor : favors) {
          if(favor.has("destination")) {
            ParseGeoPoint location = favor.getParseGeoPoint("destination");
            favor.pinInBackground();
            updateFavorLocation(location.getLatitude(), location.getLongitude());
          }
        }
      }
    });
  }

  private void switchToViewFavor(LatLng location) {
    final ParseGeoPoint userLocation = new ParseGeoPoint(location.latitude, location.longitude);
    ParseQuery<ParseObject> query = ParseQuery.getQuery("Favor");
    query.whereNear("destination", userLocation);
    query.setLimit(1);
    query.findInBackground(new FindCallback<ParseObject>() {
      @Override
      public void done(List<ParseObject> favors, ParseException e) {
        for(ParseObject favor : favors) {
          Intent intent = new Intent(HomeLocationActivity.this, ViewFavor.class);
          intent.putExtra("favorId", favor.getObjectId());
          startActivity(intent);
        }
      }
    });
  }
}
