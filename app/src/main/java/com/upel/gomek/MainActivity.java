package com.upel.gomek;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.upel.gomek.Common.Common;
import com.upel.gomek.model.User;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    RelativeLayout rootlayout;
    Button loginbtn, regisbtn;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/DroidSans.ttf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("users");

        loginbtn = (Button)findViewById(R.id.loginbtn);
        regisbtn = (Button)findViewById(R.id.regisbtn);
        rootlayout = (RelativeLayout)findViewById(R.id.rootlayout);

        regisbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterDialog();
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();
            }
        });
    }

    private void showLoginDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Login");
        dialog.setMessage("Gunakan email untuk login");

        LayoutInflater inflater = LayoutInflater.from(this);
        final View loginlayout = inflater.inflate(R.layout.loginlayout, null);

        final MaterialEditText edtemail = loginlayout.findViewById(R.id.edtemail);
        final MaterialEditText edtpass = loginlayout.findViewById(R.id.edtpass);

        dialog.setView(loginlayout);

        dialog.setPositiveButton("Masuk", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        loginbtn.setEnabled(false);

                        if (TextUtils.isEmpty(edtemail.getText().toString())) {
                            Snackbar.make(rootlayout, "Masukkan email anda", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        if (TextUtils.isEmpty(edtpass.getText().toString())) {
                            Snackbar.make(rootlayout, "Masukkan password", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        if (edtpass.getText().toString().length() < 7) {
                            Snackbar.make(rootlayout, "Password terlalu pendek!", Snackbar.LENGTH_SHORT).show();
                            return;
                        }

                        final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
                        waitingDialog.show();
                        auth.signInWithEmailAndPassword(edtemail.getText().toString(),edtpass.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waitingDialog.dismiss();
                                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance()
                                .getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Common.currentUser = dataSnapshot.getValue(User.class);
                                        startActivity(new Intent(MainActivity.this,Home.class));
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Snackbar.make(rootlayout, "Login Gagal"+e.getMessage(), Snackbar.LENGTH_SHORT).show();

                                loginbtn.setEnabled(true);
                            }
                        });
                    }
                });
        dialog.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    private void showRegisterDialog(){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Registrasi");
        dialog.setMessage("Gunakan email untuk mendaftar");

        LayoutInflater inflater = LayoutInflater.from(this);
        View regislayout = inflater.inflate(R.layout.regislayout, null);

        final MaterialEditText edtemail = regislayout.findViewById(R.id.edtemail);
        final MaterialEditText edtpass = regislayout.findViewById(R.id.edtpass);
        final MaterialEditText edtnama = regislayout.findViewById(R.id.edtnama);
        final MaterialEditText edttelp = regislayout.findViewById(R.id.edttelp);
        final MaterialEditText edtjam = regislayout.findViewById(R.id.edtjam);
        final MaterialEditText edtmek = regislayout.findViewById(R.id.edtmek);

        dialog.setView(regislayout);

        dialog.setPositiveButton("Daftar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if(TextUtils.isEmpty(edtemail.getText().toString())){
                    Snackbar.make(rootlayout,"Masukkan email anda",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edtpass.getText().toString())){
                    Snackbar.make(rootlayout,"Masukkan password",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edtnama.getText().toString())){
                    Snackbar.make(rootlayout,"Masukkan nama bengkel",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edttelp.getText().toString())){
                    Snackbar.make(rootlayout,"Masukkan nomor telepon anda",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edtjam.getText().toString())) {
                    Snackbar.make(rootlayout, "Jam buka bengkel", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edtmek.getText().toString())) {
                    Snackbar.make(rootlayout, "Masukkan daftar nama mekanik", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(edtpass.getText().toString().length() < 7){
                    Snackbar.make(rootlayout, "Password terlalu pendek!",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                auth.createUserWithEmailAndPassword(edtemail.getText().toString(),edtpass.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User();
                                user.setEmail(edtemail.getText().toString());
                                user.setPassword(edtpass.getText().toString());
                                user.setNama(edtnama.getText().toString());
                                user.setTelp(edttelp.getText().toString());
                                user.setJamBuka(edtjam.getText().toString());
                                user.setDaftarMekanik(edtmek.getText().toString());
                                user.setAvatarUrl("");
                                user.setRate("0");

                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Snackbar.make(rootlayout, "Registrasi berhasil!",Snackbar.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(rootlayout, "Gagal mendaftar"+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                        return;
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootlayout, "Gagal"+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                });
            }
        });
        dialog.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }
}
