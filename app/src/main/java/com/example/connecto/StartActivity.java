package com.example.connecto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StartActivity extends AppCompatActivity {
    private FirebaseAuth mAuth ;
    private DatabaseReference RootRef ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mAuth=FirebaseAuth.getInstance();


    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        RootRef= FirebaseDatabase.getInstance().getReference();
        if(currentUser==null) {
            sendUserToLoginActivity();
        }
        else
        {
            verifyUserExistance();
        }
    }

    private void verifyUserExistance() {
        String currentUserID= mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("Name").exists()))
                {
                    Toast.makeText(StartActivity.this,"Welcome"+dataSnapshot.child("Name").toString(),Toast.LENGTH_SHORT).show();
                    sendUserToSdpActivity();
                }
                else
                {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void sendUserToSdpActivity() {
        Intent sdpActivity = new Intent(StartActivity.this,SdpActivity.class);
        sdpActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(sdpActivity);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent loginActivity = new Intent(StartActivity.this,LoginActivity.class);
        loginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(loginActivity);
        finish();
    }

    private void sendUserToSettingsActivity() {
        Intent settingsActivity = new Intent(StartActivity.this,SettingsActivity.class);
        startActivity(settingsActivity);
    }
}