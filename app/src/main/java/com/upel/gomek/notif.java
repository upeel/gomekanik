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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class notif extends AppCompatActivity {

    Button mOke;
    MediaPlayer mediaPlayer;
    IFCMService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif);

        mService = Common.getFCMService();
        mOke = (Button)findViewById(R.id.accbtn);

        mOke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotif(Common.customerId);
            }
        });

        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void sendNotif(String customerId) {
        DatabaseReference token = FirebaseDatabase.getInstance().getReference("token");
        token.orderByKey().equalTo(customerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                    Token token = postSnapShot.getValue(Token.class);

                    Notification notif = new Notification("Siap","Pesanan Diterima!");
                    Sender content = new Sender(token.getToken(), notif);

                    mService.sendMessage(content).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body().success == 1)
                                Toast.makeText(notif.this, "Okay", Toast.LENGTH_LONG).show();
                                finish();
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
