package com.upel.gomek;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.upel.gomek.Common.Common;
import com.upel.gomek.Helper.DirectionJSONParser;
import com.upel.gomek.remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Tracking extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    private String customerId = "";

    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICES_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private LinearLayout mPelangganInfo;
    private ImageView mPotoPelanggan;
    private TextView mNamaPelanggan, mTelpPelanggan;
    DatabaseReference bengkel;
    GeoFire geofire;
    Marker mCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mPelangganInfo = (LinearLayout) findViewById(R.id.pelangganInfo);
        mPotoPelanggan = (ImageView) findViewById(R.id.potoPelanggan);
        mNamaPelanggan = (TextView) findViewById(R.id.namaPelanggan);
        mTelpPelanggan = (TextView) findViewById(R.id.telpPelanggan);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        bengkel = FirebaseDatabase.getInstance().getReference("bengkel");
        geofire = new GeoFire(bengkel);
        setUpLocation();
        getAssignedCustomer();
    }

    private void getAssignedCustomer() {
        String bengkelId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustRef = FirebaseDatabase.getInstance().getReference("bengkel").child(bengkelId).child("layananId");
        assignedCustRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerLocation();
                    getAssignedCustInfo();
                }
                else {
                    customerId = "";
                    if(pesanan != null){
                        pesanan.remove();
                    }
                    mPelangganInfo.setVisibility(View.GONE);
                    mNamaPelanggan.setText("");
                    mTelpPelanggan.setText("");
                    mPotoPelanggan.setImageResource(R.drawable.ic_face_black_24dp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getAssignedCustInfo(){
        mPelangganInfo.setVisibility(View.VISIBLE);
        DatabaseReference mPelangganDatabase = FirebaseDatabase.getInstance().getReference("customer").child(customerId);
        mPelangganDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("nama")!=null){
                        mNamaPelanggan.setText(map.get("nama").toString());
                    }
                    if(map.get("telp")!=null){
                        mTelpPelanggan.setText(map.get("telp").toString());
                    }
                    if(map.get("avatarUrl")!=null){
                        Glide.with(getApplication()).load(map.get("avatarUrl").toString()).into(mPotoPelanggan);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    Marker pesanan;
    private void getAssignedCustomerLocation() {
        DatabaseReference assignedCustLocationRef = FirebaseDatabase.getInstance().getReference("lokasiPesanan")
                .child(customerId).child("l");
        assignedCustLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng custLatLng = new LatLng(locationLat, locationLng);
                    pesanan =  mMap.addMarker(new MarkerOptions().position(custLatLng).title("Lokasi Pesanan"));
                    getRouteToMarker(custLatLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getRouteToMarker(LatLng custLatLng) {
        Routing routing = new Routing.Builder()
                .key("AIzaSyBfci3uT02NAOAdVthnQOJnP1rrAB-f9yE")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), custLatLng)
                .build();
        routing.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buildLocationCallback();
                    buildLocationRequest();
                   displayLocation();
                }

                break;
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            buildLocationRequest();
            buildLocationCallback();
            displayLocation();
        }
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Common.mLastLocation = location;
                }
                displayLocation();
            }
        };
    }

    private void buildLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }


    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Common.mLastLocation = location;

                if (Common.mLastLocation != null) {
                        final double latitude = Common.mLastLocation.getLatitude();
                        final double longitude = Common.mLastLocation.getLongitude();

                        geofire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (mCurrent != null)
                                    mCurrent.remove();
                                mCurrent = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.bengkel)).position(new LatLng(latitude, longitude)).title("Lokasi Anda"));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
                            }
                        });
                } else {
                    Log.d("Error", "Tidak mendapatkan lokasi anda");
                }
            }
        });
    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildLocationCallback();
        buildLocationRequest();
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
    }

}
