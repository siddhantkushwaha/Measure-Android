package com.measure.siddhantkushwaha.measure;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;
import com.measure.siddhantkushwaha.measure.adapter.PlacesAutoCompleteAdapter;
import com.measure.siddhantkushwaha.measure.api.PlacesAPI;
import com.measure.siddhantkushwaha.measure.pojo.SearchedPlace;
import com.measure.siddhantkushwaha.measure.utitlities.AreaConverter;

import java.util.ArrayList;
import java.util.List;

public class mapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener {

    private String TAG = "MAPS_ACTIVITY";

    private GoogleMap mMap;
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PolygonOptions polygonOptions;
    private LatLng mDefaultLocation;
    private Location mLastKnownLocation;
    private Marker marker;

    private PolylineOptions polylineOptions;
    private List<LatLng> polylinelist;
    private Polyline polyline;

    private TextView areaAcres;
    private TextView areaSqMeter;
    private TextView areaSqFoot;

    private FrameLayout frameLayout;
    private Button drawButton;
    //    private Button areaButton;
    private Boolean drawable;

    // TODO, get the saved plotting in savedLatLngs
    private List<LatLng> savedLatLngs;

    private SearchView searchView;
    private TextView titleTextView;

    private AutoCompleteTextView autoCompleteTextView;
    private PlacesAutoCompleteAdapter placesAutoCompleteAdapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        View mapView = mapFragment.getView();
        if (mapView != null &&
                mapView.findViewById(1) != null) {
            View locationButton = ((View) mapView.findViewById(1).getParent()).findViewById(2);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 200);
        }

        mapFragment.getMapAsync(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.argb(255, 0, 124, 214));

        polylinelist = new ArrayList<>();

        drawable = false;
        drawButton = findViewById(R.id.drawButton);
        drawButton.setText("Plot");
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                drawable = !drawable;
                if (drawButton.getText().toString().equals("Move")) {

                    drawButton.setText("Plot");

                    areaAcres.setText("Acres");
                    areaSqMeter.setText("Sq. Meter");
                    areaSqFoot.setText("Sq. Feet");

                    undoChanges();

                } else {

                    drawButton.setText("Move");
                }
            }
        });

        frameLayout = findViewById(R.id.fram_map);
        frameLayout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (drawable) {
                    float x = event.getX();
                    float y = event.getY();
                    Point point = new Point(Math.round(x), Math.round(y));
                    LatLng latLng = mMap.getProjection().fromScreenLocation(point);

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initiatePlotting();
                            sketch(latLng);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            sketch(latLng);
                            break;
                        case MotionEvent.ACTION_UP:
                            drawMap();
                            findAndShowArea(polygonOptions.getPoints());
                            break;
                    }
                }
                return drawable;
            }
        });

        savedLatLngs = new ArrayList<>();

        areaAcres = findViewById(R.id.acreTextView);
        areaSqMeter = findViewById(R.id.sqMeterTextView);
        areaSqFoot = findViewById(R.id.sqFeetTextView);

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        placesAutoCompleteAdapter = new PlacesAutoCompleteAdapter(mapsActivity.this, R.layout.layout_autocomplete);
        autoCompleteTextView.setAdapter(placesAutoCompleteAdapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.i(TAG, "HERE");
                SearchedPlace searchedPlace = (SearchedPlace) parent.getItemAtPosition(position);
                Log.i(TAG, searchedPlace.getPlace_id());
                moveMapToLocation(searchedPlace);
            }
        });

        PlacesAPI placesAPI = new PlacesAPI(this);
        placesAPI.findPlaceDetails("ChIJbU60yXAWrjsR4E9-UejD3_g");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onPolygonClick(Polygon polygon) {

    }

    @Override
    public void onPolylineClick(Polyline polyline) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
//        MapStyleOptions mapStyleOptions = new MapStyleOptions();

        mMap.setMapType(mMap.MAP_TYPE_HYBRID);

        updateLocationUI();
        getDeviceLocation();
    }

    private void getLocationPermission() {

        Log.i(TAG, "asking for permission");

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            updateLocationUI();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case 0: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {

        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {

                            mLastKnownLocation = (Location) task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), 15));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 15));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void moveMapToLocation(SearchedPlace searchedPlace) {

        LatLng latLng = new PlacesAPI(mapsActivity.this).findPlaceDetails(searchedPlace.getPlace_id());
        Log.i(TAG, latLng.toString());

        if (latLng == null)
            return;

        if(marker!=null)
            marker.remove();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(searchedPlace.getDescription());
        marker=mMap.addMarker(markerOptions);
    }

    private void initiatePlotting() {

        mMap.clear();
        polylinelist.clear();
        polyline = mMap.addPolyline(polylineOptions);
    }

    private void sketch(LatLng latLng) {

        polylinelist.add(latLng);
        polyline.setPoints(polylinelist);
    }

    private void drawMap() {

        mMap.clear();

        polygonOptions = new PolygonOptions();
        polygonOptions.strokeColor(Color.argb(255, 0, 124, 214));
        polygonOptions.strokeWidth(7);
        polygonOptions.fillColor(Color.argb(100, 130, 200, 255));
        polygonOptions.addAll(polylinelist);

        mMap.addPolygon(polygonOptions);
    }

    private void undoChanges() {

        mMap.clear();
        // TODO get the save plotting again
    }

    private void findAndShowArea(List<LatLng> polygonList) {

        double sqMeter = SphericalUtil.computeArea(polygonList);
        double acre = AreaConverter.toAcres(sqMeter);
        double sqFeet = AreaConverter.toSqFoot(sqMeter);

        areaAcres.setText(String.valueOf(Math.round(acre * 100.0) / 100.0));
        areaSqMeter.setText(String.valueOf(Math.round(sqMeter * 100.0) / 100.0));
        areaSqFoot.setText(String.valueOf(Math.round(sqFeet * 100.0) / 100.0));
    }
}