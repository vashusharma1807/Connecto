package com.example.connecto;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    SignInButton signInGoogle;
    private Button anonymousSignin ;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 1;

    private User user ;

    private FirebaseAuth mAuth ;
    private ProgressDialog loadingBar;
    private DatabaseReference UserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initialize();

        signInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        anonymousSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anonymousSingIn();
            }
        });

    }



    protected void initialize(){
        signInGoogle = findViewById(R.id.sign_in_google);
        anonymousSignin = findViewById(R.id.sign_in_anonymous);

        user = new User();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        mAuth = FirebaseAuth.getInstance();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //updateUI(account);

        Log.d("Setup","done");

        loadingBar=new ProgressDialog(this);
        }

    //TODO : --------------------------------------------------------------------------
    //TODO : Logout ---> FirebaseAuth.getInstance().signOut();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            Log.d("Msg",data.toString());
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d("Msg",result.toString());

            if(result.isSuccess()){
                GoogleSignInAccount account=result.getSignInAccount();
                handleSignInResult(account);
            }
            else{
                Log.e("Msg","Result Failed");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(this, authResult -> {

                    handleSignInResult(acct);
                })
                .addOnFailureListener(this, e -> Toast.makeText(LoginActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show());
    }

    private void handleSignInResult(GoogleSignInAccount acct){

        String personName = acct.getDisplayName();
        Uri personPhoto = acct.getPhotoUrl();
        String personEmail = acct.getEmail();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(this, authResult -> {

                    addUserData(personName,personEmail,personPhoto);

                })
                .addOnFailureListener(this, e -> Toast.makeText(LoginActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show());

    }

    // -------------------------------------------------------------------------- //

    private void addUserData(String name, String email, Uri image) {

        String currentUserId=mAuth.getCurrentUser().getUid();

        UserRef.child(currentUserId).child("Email").setValue(email);
        UserRef.child(currentUserId).child("Image").setValue(image.toString());
        UserRef.child(currentUserId).child("Name").setValue(name);

    }

    private void anonymousSingIn() {
        Call<ResponseBody> call = RetrofitClient.getInstance().getMyApi().getsuperHeroes();
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                parseResponse(response);
                gotoProfile(user);
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Error",t.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), "Error in Getting Data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void parseResponse(Response<ResponseBody> response) {
        ResponseBody responseBody = response.body();

        Log.e("User Data", responseBody.toString());

        try {
            JSONObject jsonObject = new JSONObject(response.body().string());
            JSONArray result = jsonObject.getJSONArray("results");
            JSONObject result1 = result.getJSONObject(0);
            JSONObject name = result1.getJSONObject("name");
            JSONObject loginDetails = result1.getJSONObject("login");
            user.setName(name.get("first").toString()+" "+name.get("last").toString());
            user.setEmail(result1.get("email").toString());
            JSONObject id = result1.getJSONObject("id");
            user.setId(id.get("value").toString());
            JSONObject picture = result1.getJSONObject("picture");
            user.setImage(Uri.parse(picture.get("large").toString()));
            LoginToFirebase(user.getEmail(),user.getName(),loginDetails.get("password").toString(),user.getId(),user.getImage());

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

    }


    private void gotoProfile(User user){
        Log.d("UserInfo to Settings",user.getName()+user.getEmail()+user.getId()+user.getImage().toString());
        Intent intent=new Intent(LoginActivity.this,SettingsActivity.class);
        intent.putExtra("name",user.getName());
        intent.putExtra("email",user.getEmail());
        intent.putExtra("id",user.getId());
        intent.putExtra("photo",user.getImage());
        startActivity(intent);
    }


    private void LoginToFirebase(String email , String name , String password , String id , Uri image){
        password = password+password+password;

        loadingBar.setTitle("Signing In:");
        loadingBar.setMessage("Please Wait...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.setCancelable(true);
        loadingBar.show();

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d("Setup","1st loop");
                    addUserData(name,email,image);
                }
                else {
                    String error = task.getException().toString();
                    Log.e("Setup",error);
                    Toast.makeText( LoginActivity.this, "Error:"+error, Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }
            }


        });

    }


}