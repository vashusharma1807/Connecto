package com.example.connecto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SdpActivity extends AppCompatActivity {

    private RecyclerView activeUserList ;
    private DatabaseReference UsersRef, ActiveRef;
    private String currentUserId;
    private FirebaseAuth mAuth ;
    private Button createSdpCall;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdp);

        activeUserList = (RecyclerView) findViewById(R.id.active_user_list);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        ActiveRef = FirebaseDatabase.getInstance().getReference().child("Active");
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        createSdpCall = (Button) findViewById(R.id.create_sdp_call);
        // TODO :  dfmwekfkewf
        loadList();

        createSdpCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewCall();
            }
        });

    }

    private void startNewCall() {

        Log.d("1","In fun");

        UsersRef.child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        Log.d("1","In Data Change");
                        String retrieveUserName=null;
                        String retrievesStatus=null;
                        String retrieveProfileImage = null;

                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("Name") && (dataSnapshot.hasChild("Image"))))
                        {
                            retrieveUserName = dataSnapshot.child("Name").getValue().toString();
                            retrievesStatus = dataSnapshot.child("Email").getValue().toString();
                            retrieveProfileImage = dataSnapshot.child("Image").getValue().toString();
                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            retrievesStatus = dataSnapshot.child("status").getValue().toString();
                        }

                        ActiveRef.child(currentUserId).child("Email").setValue(retrievesStatus);
                        ActiveRef.child(currentUserId).child("Image").setValue(retrieveProfileImage);
                        ActiveRef.child(currentUserId).child("Name").setValue(retrieveUserName);

                        Log.d("1","Added");

                        Intent callIntent = new Intent(SdpActivity.this,CallActivity.class);
                        callIntent.putExtra("peerId","Everyone");
                        startActivity(callIntent);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

    }


    @Override
    public void onStart() {
        super.onStart();
        activeUserList.setLayoutManager(new LinearLayoutManager(this));

    }

    public void loadList(){

        FirebaseRecyclerOptions<User> options = new  FirebaseRecyclerOptions.Builder<User>()
                .setQuery(ActiveRef,User.class)
                .build();

        FirebaseRecyclerAdapter<User,ChatsViewHolder> adapter=new FirebaseRecyclerAdapter<User, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull User model)
            {

                final String userIds = getRef(position).getKey();
                final String[] userImage={"Default Image"};

                UsersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists())
                        {
                            final String profileName = dataSnapshot.child("Name").getValue().toString();
                            final String profileStatus = dataSnapshot.child("Email").getValue().toString();
                            if(dataSnapshot.hasChild("Image"))
                            {
                                userImage[0] = dataSnapshot.child("Image").getValue().toString();

                                holder.userName.setText(profileName);

                                Picasso.get().load(userImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);

                            }
                            else
                            {

                                holder.userName.setText(profileName);

                                holder.profileImage.setImageResource(R.drawable.profile_image);
                            }
                            holder.userOnlineState.setVisibility(View.VISIBLE);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view)
                                {
                                    // TODO -- Join the call
                                    Intent intent = new Intent(SdpActivity.this,CallActivity.class);
                                    intent.putExtra("peerId",userIds.toString());
                                    startActivity(intent);
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }


            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(SdpActivity.this).inflate(R.layout.user_display_layout,parent,false);
                return new ChatsViewHolder(view);
            }
        };
        activeUserList.setAdapter(adapter);
        adapter.startListening();

    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        TextView userName , userStatus;
        CircleImageView profileImage ;
        ImageView userOnlineState;
        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            userOnlineState=itemView.findViewById(R.id.user_online_status);
        }
    }


}