package com.upel.gomek.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.upel.gomek.Common.Common;
import com.upel.gomek.model.Token;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        updateTokenToServer(refreshedToken);
    }
    private void updateTokenToServer(String refreshedToken){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("token");

        Token token = new Token(refreshedToken);
        if(FirebaseAuth.getInstance().getCurrentUser() !=null)
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }
}
