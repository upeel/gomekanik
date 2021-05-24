package com.upel.gomek;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.upel.gomek.Common.Common;
import com.upel.gomek.model.FCMResponse;
import com.upel.gomek.model.Notification;
import com.upel.gomek.model.Sender;
import com.upel.gomek.model.Token;
import com.upel.gomek.remote.IFCMService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    private String customerId = "";

    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICES_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    DatabaseReference onlineRef, currentUserRef;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    DatabaseReference bengkel;
    GeoFire geofire;
    private LinearLayout mPelangganInfo;
    private ImageView mPotoPelanggan;
    private TextView mNamaPelanggan, mTelpPelanggan, mRincian;
    private Button mCancel;

    Marker mCurrent;
    private Switch mActiveSwitch;
    SupportMapFragment mapFragment;
    IFCMService ifcmService;


    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        polylines = new ArrayList<>();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationHeaderView = navigationView.getHeaderView(0);
        TextView txtNama = (TextView) navigationHeaderView.findViewById(R.id.txtNamaBengkel);
        CircleImageView imgAvatar = (CircleImageView) navigationHeaderView.findViewById(R.id.avatar);
        TextView txtStar = (TextView) navigationHeaderView.findViewById(R.id.txtStar);

        txtNama.setText(Common.currentUser.getNama());
        txtStar.setText(Common.currentUser.getRate());
        if(Common.currentUser.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currentUser.getAvatarUrl())) {
            Picasso.with(this).load(Common.currentUser.getAvatarUrl()).into(imgAvatar);
        }
        mPelangganInfo = (LinearLayout) findViewById(R.id.pelangganInfo);
        mPotoPelanggan = (ImageView) findViewById(R.id.potoPelanggan);
        mNamaPelanggan = (TextView) findViewById(R.id.namaPelanggan);
        mTelpPelanggan = (TextView) findViewById(R.id.telpPelanggan);
        mRincian = (TextView) findViewById(R.id.rincian);
        mCancel = (Button)findViewById(R.id.cancel);

        ifcmService = Common.getFCMService();
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentUserRef = FirebaseDatabase.getInstance().getReference("bengkel")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUserRef.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mActiveSwitch = (Switch) findViewById(R.id.activeswitch);
        mActiveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    FirebaseDatabase.getInstance().goOnline();

                    if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    buildLocationCallback();
                    buildLocationRequest();
                    fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
                    displayLocation();
                    Snackbar.make(mapFragment.getView(), "Anda sedang online", Snackbar.LENGTH_SHORT).show();
                } else {
                    FirebaseDatabase.getInstance().goOffline();
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    mCurrent.remove();
                    Snackbar.make(mapFragment.getView(), "Anda sedang offline", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        bengkel = FirebaseDatabase.getInstance().getReference("bengkel");
        geofire = new GeoFire(bengkel);
        setUpLocation();
        getAssignedCustomer();
        updateFirebaseToken();

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
                    mRincian.setText("");
                    mPotoPelanggan.setImageResource(R.drawable.ic_face_black_24dp);
                    mCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            cancelBooking(customerId);
                            erasePolylines();
                            pesanan.remove();
                            mPelangganInfo.removeAllViews();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void cancelBooking(String customerId) {
        DatabaseReference token = FirebaseDatabase.getInstance().getReference("token");
        token.orderByKey().equalTo(customerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                    Token token = postSnapShot.getValue(Token.class);

                    Notification notif = new Notification("Maaf","Maaf sedang penuh. Belum dapat menerima pesanan anda");
                    Sender content = new Sender(token.getToken(), notif);

                    ifcmService.sendMessage(content).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body().success == 1)
                                Toast.makeText(Home.this, "Telah dibatalkan", Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(Home.this,"Gagal!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.e("ERROR" ,t.getMessage());
                        }
                    });
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
        DatabaseReference rincian = FirebaseDatabase.getInstance().getReference("lokasiPesanan").child(customerId);
        rincian.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("rincian")!=null){
                        mRincian.setText(map.get("rincian").toString());
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

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("token");

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        buildLocationCallback();
                        buildLocationRequest();
                        if (mActiveSwitch.isChecked()) displayLocation();
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
            if (mActiveSwitch.isChecked()) displayLocation();
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
                    if (mActiveSwitch.isChecked()) {
                        final double latitude = Common.mLastLocation.getLatitude();
                        final double longitude = Common.mLastLocation.getLongitude();

                        geofire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (mCurrent != null)
                                    mCurrent.remove();
                                mCurrent = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.bengkel)).position(new LatLng(latitude, longitude)).title("Lokasi Anda"));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
                                rotateMarker(mCurrent, 360, mMap);
                            }
                        });
                    }
                } else {
                    Log.d("Error", "Tidak mendapatkan lokasi anda");
                }
            }
        });
    }

    private void rotateMarker(final Marker mCurrent, final int i, GoogleMap mMap) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = mCurrent.getRotation();
        final long duration = 1500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                float rot = t * i + (1 - t) * startRotation;
                mCurrent.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.selesai) {
            sendNotif(customerId);
            erasePolylines();
            pesanan.remove();
            mPelangganInfo.removeAllViews();
        } else if (id == R.id.update) {
            Intent intent = new Intent(Home.this, updateinfo.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.changepass) {
            showDialogChangePass();

        } else if (id == R.id.logout) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendNotif(String customerId) {
        DatabaseReference token = FirebaseDatabase.getInstance().getReference("token");
        token.orderByKey().equalTo(customerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                    Token token = postSnapShot.getValue(Token.class);

                    Notification notif = new Notification("Selesai","Telah selesai!");
                    Sender content = new Sender(token.getToken(), notif);

                    ifcmService.sendMessage(content).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body().success == 1)
                                Toast.makeText(Home.this, "Terima Kasih!", Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(Home.this,"Gagal!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.e("ERROR" ,t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showDialogChangePass() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("GANTI PASSWORD");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.changepass, null);

        final MaterialEditText edtpassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtpassword);
        final MaterialEditText edtnewpass = (MaterialEditText) layout_pwd.findViewById(R.id.edtnewpass);
        final MaterialEditText edtnewpass1 = (MaterialEditText) layout_pwd.findViewById(R.id.edtnewpass1);

        alertDialog.setView(layout_pwd);

        alertDialog.setPositiveButton("GANTI PASSWORD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();

                if (edtnewpass.getText().toString().equals(edtnewpass1.getText().toString())) {
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    AuthCredential credential = EmailAuthProvider.getCredential(email, edtpassword.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                FirebaseAuth.getInstance().getCurrentUser().updatePassword(edtnewpass1.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Map<String, Object> password = new HashMap<>();
                                                    password.put("password", edtnewpass1.getText().toString());

                                                    DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
                                                    users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(password)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful())
                                                                        Toast.makeText(Home.this, "Password Berhasil Diganti!", Toast.LENGTH_SHORT).show();
                                                                    else
                                                                        Toast.makeText(Home.this, "Password berhasil diganti tetapi tidak diupdate ke database", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                } else {
                                                    Toast.makeText(Home.this, "Gagal ganti password", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                waitingDialog.dismiss();
                                Toast.makeText(Home.this, "Password Lama Salah", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    waitingDialog.dismiss();
                    Toast.makeText(Home.this, "Password tidak sesuai", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Home.this, MainActivity.class);
        startActivity(intent);
        finish();
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

            Toast.makeText(getApplicationContext(),"Rute "+ (i+1) +": jarak - "+ route.get(i).getDistanceValue()+": durasi waktu - "+ route.get(i).getDurationValue(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }
}
