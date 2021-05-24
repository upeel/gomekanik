package com.upel.gomek;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.upel.gomek.Common.Common;
import com.upel.gomek.model.FCMResponse;
import com.upel.gomek.model.Notification;
import com.upel.gomek.model.Sender;
import com.upel.gomek.model.Token;
import com.upel.gomek.remote.IFCMService;
import com.upel.gomek.remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCall extends AppCompatActivity {
    TextView txtTime, txtAddress, txtDistance;
    Button accbtn, decbtn;

    String customerId;
    MediaPlayer mediaPlayer;
    IGoogleAPI mService;
    IFCMService mFCMService;

    String lat,lng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);

        mService = Common.getGoogeAPI();
        mFCMService = Common.getFCMService();

        accbtn = (Button)findViewById(R.id.accbtn);
        decbtn = (Button)findViewById(R.id.decbtn);

        txtAddress = (TextView)findViewById(R.id.txtAddress);
        txtDistance = (TextView)findViewById(R.id.txtDistance);
        txtTime = (TextView)findViewById(R.id.txtTime);

        decbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(customerId))
                    cancelBooking(customerId);
            }
        });
        accbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(CustomerCall.this,Tracking.class);
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
               startActivity(intent);
               finish();
            }
        });


        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if(getIntent() !=null){
            lat = getIntent().getStringExtra("lat");
            lng = getIntent().getStringExtra("lng");
            customerId = getIntent().getStringExtra("customer");
            getDirection(lat,lng);
        }
    }

    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);

        Notification notification = new Notification("Maaf!", "Maaf, pihak bengkel belum dapat menerima pesanan anda");
        Sender sender = new Sender(token.getToken(),notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success == 1){
                    Toast.makeText(CustomerCall.this, "Ditunda", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void getDirection(String lat, String lng) {
        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+"mode=driving&"+
                    "transit_routing_prefeerences=less_driving&"+"origin="+Common.mLastLocation.getLatitude()+
                    ","+Common.mLastLocation.getLongitude()+"&"+"destination="+lat+","+lng+"&"+"key"+getResources()
                    .getString(R.string.google_direction_api);
            Log.d("UPEL",requestApi);
            mService.getPath(requestApi).enqueue(new Callback<String>(){

                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");
                        JSONObject object = routes.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");
                        JSONObject legsObject = legs.getJSONObject(0);
                        JSONObject distance = legsObject.getJSONObject("distance");
                        txtDistance.setText(distance.getString("text"));
                        JSONObject time = legsObject.getJSONObject("duration");
                        txtTime.setText(time.getString("text"));
                        String address = legsObject.getString("end_address");
                        txtAddress.setText(address);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(CustomerCall.this, "" +t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        if(mediaPlayer.isPlaying())
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(mediaPlayer !=null && !mediaPlayer.isPlaying())
        mediaPlayer.start();
    }
}
