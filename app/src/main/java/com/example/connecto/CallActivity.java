package com.example.connecto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.webkit.WebViewAssetLoader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.IOUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public class CallActivity extends AppCompatActivity {

    private String currentUserId , friendUserId;
    private Boolean isPeerConnected = false , isAudio = true , isVideo= true ;

    private ImageView toggleAudioBtn , toggleVideoBtn,acceptBtn , rejectBtn ;
    private Button callBtn ;
    private WebView webView;
    private RelativeLayout callLayout , inputLayout;
    private LinearLayout callControlLayout;
    private TextView incomingCallTxt ;

    private final String TAG = "TEST";
    private PermissionRequest mPermissionRequest;

    private DatabaseReference ActiveRef  ;
    private FirebaseAuth mAuth ;

    private String uniqueId;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        ActiveRef = FirebaseDatabase.getInstance().getReference().child("Active");

        if(!permissionGranted()){
            askPermission();
        }

        initialize();

        toggleAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAudio = !isAudio;
                callJavascriptFunction("javascript:toggleAudio(\"${isAudio}\")");
                if(isAudio)
                    toggleAudioBtn.setImageResource(R.drawable.ic_baseline_mic_24);
                else
                   toggleAudioBtn.setImageResource(R.drawable.ic_baseline_mic_off_24);
            }
        });

        toggleAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVideo = !isVideo;
                callJavascriptFunction("javascript:toggleVideo(\"${isVideo}\")");
                if(isVideo)
                    toggleVideoBtn.setImageResource(R.drawable.ic_baseline_videocam_24);
                else
                    toggleVideoBtn.setImageResource(R.drawable.ic_baseline_videocam_off_24);

            }
        });

        if(friendUserId!="Everyone") {

            callBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendCallRequest(friendUserId);
                }
            });
        }
        else{
            inputLayout.setVisibility(View.INVISIBLE);
        }


        setUpWebView();

    }

    private void sendCallRequest(String friendUserId) {
        if(!isPeerConnected){
            Toast.makeText(this, "You are not Connected, please check the internet", Toast.LENGTH_SHORT).show();
            return ;
        }

        ActiveRef.child(friendUserId).child("incoming").setValue(currentUserId);
        ActiveRef.child(friendUserId).child("isAvailable").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue().equals("true")){
                    listenForConnId();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void listenForConnId() {
        ActiveRef.child(friendUserId).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue().equals(null))
                    return ;
                switchToControls();
                callJavascriptFunction("javascript:startCall(\"${snapshot.getValue()}\")");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void initialize(){
        toggleAudioBtn = (ImageView) findViewById(R.id.toggleAudioBtn);
        toggleVideoBtn = (ImageView) findViewById(R.id.toggleVideoBtn);
        callBtn = (Button) findViewById(R.id.callBtn);
        acceptBtn = (ImageView) findViewById(R.id.acceptBtn);
        rejectBtn = (ImageView) findViewById(R.id.rejectBtn);

        webView = (WebView) findViewById(R.id.webView);
        callLayout = (RelativeLayout) findViewById(R.id.callLayout);
        incomingCallTxt = (TextView) findViewById(R.id.incomingCallTxt);
        inputLayout = (RelativeLayout) findViewById(R.id.inputLayout);
        callControlLayout = (LinearLayout) findViewById(R.id.callControlLayout);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        friendUserId = getIntent().getExtras().get("peerId").toString();

    }

    private void askPermission() {

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},1);
    }

    private boolean permissionGranted() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
            return false;
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED)
            return false;

        return true;
    }

    private void setUpWebView() {

        webView.setWebChromeClient(new WebChromeClient() {
            // Grant permissions for cam
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                Log.i(TAG, "onPermissionRequest");
                mPermissionRequest = request;
                final String[] requestedResources = request.getResources();
                for (String r : requestedResources) {
                    if (r.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                        // In this sample, we only accept video capture request.
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CallActivity.this)
                                .setTitle("Allow Permission to camera")
                                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        mPermissionRequest.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                                        Log.d(TAG,"Granted");
                                    }
                                })
                                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        mPermissionRequest.deny();
                                        Log.d(TAG,"Denied");
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();

                        break;
                    }
                }
            }

            @Override
            public void onPermissionRequestCanceled(PermissionRequest request) {
                super.onPermissionRequestCanceled(request);
                Toast.makeText(CallActivity.this,"Permission Denied", Toast.LENGTH_SHORT).show();
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        webView.addJavascriptInterface(new JavascriptInterface(this), "Android");

        loadVideoCall();
    }

    private void loadVideoCall() {
        //String call = "call.html";

        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);

        String output  = getUrlContents("https://drive.google.com/file/d/1ip7pBSn9BD04X4S-OvzkIU6ShL3JRECt/view?usp=sharing");

        Log.d("loadVideoCall: ", output);


        webView.loadData(output,"text/html","UTF-8");



        Log.d("loadVideoCall: ", output);


        //webView.loadData( , "text/html" , "UTF-8");

        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();
            }
        });


    }

    private String getUrlContents(String theUrl) {
        StringBuilder content = new StringBuilder();
        // Use try and catch to avoid the exceptions
        try
        {
            URL url = new URL(theUrl); // creating a url object
            URLConnection urlConnection = url.openConnection(); // creating a urlconnection object

            // wrapping the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            // reading from the urlconnection using the bufferedreader
            while ((line = bufferedReader.readLine()) != null)
            {
                content.append(line + "\n");
            }
            bufferedReader.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return content.toString();

    }

    private void initializePeer() {

        uniqueId = getUniqueID();

        callJavascriptFunction("javascript:init(\"${uniqueId}\")");
        Log.d("UseriD",currentUserId);

        ActiveRef.child(currentUserId).child("incoming").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                onCallRequest(snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void onCallRequest(Object caller) {
        if(caller==null)
            return ;

        callLayout.setVisibility(View.VISIBLE);
        incomingCallTxt.setText("$caller wants to join the call...");

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActiveRef.child(currentUserId).child("connId").setValue(uniqueId);
                ActiveRef.child(currentUserId).child("isAvailable").setValue(true);

                callLayout.setVisibility(View.GONE);
                switchToControls();
            }
        });

        rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActiveRef.child(currentUserId).child("incoming").setValue(null);
                callLayout.setVisibility(View.GONE);
            }
        });


    }

    private void switchToControls() {
        inputLayout.setVisibility(View.GONE);
        callControlLayout.setVisibility(View.VISIBLE);
    }

    private void callJavascriptFunction(String functionString){

        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript("javascript:executeNext()",null);
            }
        });
    }

    public void onPeerConnected() {
        isPeerConnected=true;
    }

    private String  getUniqueID(){
        return UUID.randomUUID().toString();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
     protected void onDestroy() {
        ActiveRef.child(currentUserId).setValue(null);
        webView.loadUrl("about:blank");
        super.onDestroy();
    }

}