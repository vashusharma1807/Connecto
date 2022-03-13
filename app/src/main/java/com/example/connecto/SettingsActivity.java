package com.example.connecto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class SettingsActivity extends AppCompatActivity {

    private String username,userid,useremail;
    private Uri userimage;

    private TextView userProfilename , userProfileEmail ,userProfileId;
    private ImageView userProfileImage;
    private Button toSdpActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initialize();

        RetrieveUserInfo();

        toSdpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this,SdpActivity.class);
                startActivity(intent);
            }
        });

    }

    protected void initialize(){
        try {
            useremail=getIntent().getExtras().get("email").toString();
        }
        catch (Exception e){
            useremail="Email Not given";
        }

        try {
            userid=getIntent().getExtras().get("id").toString();
        }
        catch (Exception e){
            userid="ID Not given";
        }
        try {
            username=getIntent().getExtras().get("name").toString();
        }
        catch (Exception e){
            username="Name Not given";
        }
        try {

            userimage= (Uri) getIntent().getExtras().get("photo");
        }
        catch (Exception e){
            userimage=null;
        }



        userProfilename=(TextView) findViewById(R.id.visit_user_name);
        userProfileEmail=(TextView) findViewById(R.id.visit_user_email);
        userProfileId=(TextView) findViewById(R.id.visit_user_id);
        toSdpActivity = (Button) findViewById(R.id.toSdp);

        userProfileImage=(ImageView) findViewById(R.id.visit_profile_image);

    }

    public void RetrieveUserInfo(){

        Picasso.get().load(userimage).placeholder(R.drawable.profile_image).into(userProfileImage);

        userProfilename.setText(username);
        userProfileEmail.setText(useremail);
        userProfileId.setText(userid);

    }
}