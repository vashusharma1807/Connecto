package com.example.connecto;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

    private List<User>listData;
    private Context context ;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance() ;
    private String locationOf="00,00";
    private DatabaseReference RootRef ;
    private  String currentUserID;
    private Location currLocation ;

    public MyAdapter(Context c , List<User> listData) {
        this.context = c;
        this.listData = listData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        User ld=listData.get(position);

        retrieve(holder, position);

    }


    private void deleteWork(int position, final ViewHolder holder) {
        Toast.makeText(context, "deleteWork", Toast.LENGTH_SHORT).show();

        try {



            Toast.makeText(context, listData.get(position).getId() , Toast.LENGTH_SHORT).show();
            final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            Toast.makeText(context, (listData.get(position).getId()) , Toast.LENGTH_SHORT).show();

            rootRef.child("Works").child( listData.get(position).getId() ).child(listData.get(position).getId())
                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(holder.itemView.getContext(), "Could not delete message", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
        catch (Exception e)
        {
            Toast.makeText(context, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }





    private void retrieve(ViewHolder holder, int position) {

        User ld=listData.get(position);

        final String profileName = ld.getName();
        final String profileStatus = ld.getEmail();
        final Uri image = ld.getImage();

        if(image!=null)
        {

            holder.userName.setText(profileName);

            Picasso.get().load(image).placeholder(R.drawable.profile_image).into(holder.profileImage);

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
            }
        });
    }


    @Override
    public int getItemCount() {
        return listData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView userName , userStatus;
        CircleImageView profileImage ;
        ImageView userOnlineState;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            userOnlineState=itemView.findViewById(R.id.user_online_status);
        }
    }
}