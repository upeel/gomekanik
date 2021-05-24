package com.upel.gomek.Service;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.upel.gomek.CustomerCall;
import com.upel.gomek.notif;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        Intent intent = new Intent(getBaseContext(), notif.class);
        startActivity(intent);
        }
    }


