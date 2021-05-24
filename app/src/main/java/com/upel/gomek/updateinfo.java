package com.upel.gomek;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class updateinfo extends AppCompatActivity {
    private EditText mNamaField, mTelpField, mJamField, mMekanikField;

    private Button mBack, mConfirm;
    private ImageView mPP;

    private FirebaseAuth mAuth;
    private DatabaseReference mBengkelDatabase;
    private String userId;
    private String mNama;
    private String mTelp;
    private String mJam;
    private String mMekanik;
    private String mpotoProfilUrl;

    private Uri resultUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updateinfo);

        mNamaField = (EditText) findViewById(R.id.nama);
        mTelpField = (EditText) findViewById(R.id.telp);
        mJamField = (EditText)findViewById(R.id.jam);
        mMekanikField = (EditText)findViewById(R.id.mekanik);
        mPP = (ImageView) findViewById(R.id.pp);

        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mBengkelDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        getUserInfo();
        mPP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserProfile();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(updateinfo.this,Home.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }
    private void getUserInfo(){
        mBengkelDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("nama")!=null){
                        mNama = map.get("nama").toString();
                        mNamaField.setText(mNama);
                    }
                    if(map.get("telp")!=null){
                        mTelp = map.get("telp").toString();
                        mTelpField.setText(mTelp);
                    }
                    if(map.get("jamBuka")!=null){
                        mJam = map.get("jamBuka").toString();
                        mJamField.setText(mJam);
                    }
                    if(map.get("daftarMekanik")!=null){
                        mMekanik = map.get("daftarMekanik").toString();
                        mMekanikField.setText(mMekanik);
                    }
                    if(map.get("avatarUrl")!=null){
                        mpotoProfilUrl = map.get("avatarUrl").toString();
                        Glide.with(getApplication()).load(mpotoProfilUrl).into(mPP);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void saveUserProfile(){
        mNama = mNamaField.getText().toString();
        mTelp = mTelpField.getText().toString();
        mJam = mJamField.getText().toString();
        mMekanik = mMekanikField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("nama", mNama);
        userInfo.put("telp", mTelp);
        userInfo.put("jam", mJam);
        userInfo.put("mekanik", mMekanik);
        mBengkelDatabase.updateChildren(userInfo);

        if(resultUri !=null){
            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("poto_profil").child(userId);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e){
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map newImage = new HashMap();
                            newImage.put("avatarUrl", uri.toString());
                            mBengkelDatabase.updateChildren(newImage);

                            finish();
                            return;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            finish();
                            return;
                        }
                    });
                }
            });
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mPP.setImageURI(resultUri);
        }
    }
}

