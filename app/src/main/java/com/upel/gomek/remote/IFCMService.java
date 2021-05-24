package com.upel.gomek.remote;

import com.upel.gomek.model.FCMResponse;
import com.upel.gomek.model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json","Authorization:key=AAAAfMF6aT8:APA91bF_iyTOIxUnwfcf5giblJ2aY-Iu-0Dvq8ay1Zr9QvPVPI2L5TxzWKdHgIIqgI-est25PuhcC51Yg3-U7ChUtS15AdW6gu-R-9962cVEDlMRUHjJoY5Vhqq5jd-zglzpU-RD85GT"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}
