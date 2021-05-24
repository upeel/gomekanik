package com.upel.gomek.Common;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.upel.gomek.model.User;
import com.upel.gomek.remote.FCMClient;
import com.upel.gomek.remote.IFCMService;
import com.upel.gomek.remote.IGoogleAPI;
import com.upel.gomek.remote.RetrofitClient;

public class Common {

    public static final int PICK_IMAGE_REQUEST = 9999;
    public static Location mLastLocation=null;

    public static User currentUser;
    public static String customerId;
    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com";

    public static IGoogleAPI getGoogeAPI(){
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }
    public static IFCMService getFCMService(){
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }
}
