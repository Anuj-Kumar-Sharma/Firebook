package com.example.anujsharma.firebook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import dataStructures.Feed;

public class AnotherUserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 3;
    private ImageView ivUserImage;
    private TextView tvUserName, tvUserEmail;
    private ImageButton ibEditImage;
    private RecyclerView rvUserProfile;

    private DatabaseReference mUserDbRef, mFeedsDbRef;
    private FirebaseAuth mAuth;
    private Uri resultUri;
    private Query userOnlyQuery;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another_user_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();

        user_id = intent.getStringExtra("feed_uid");

        ivUserImage = (ImageView) findViewById(R.id.xivAnotherUserImage);
        tvUserName = (TextView) findViewById(R.id.xtvAnotherUserName);
        tvUserEmail = (TextView) findViewById(R.id.xtvAnotherUserEmail);
        ibEditImage = (ImageButton) findViewById(R.id.xibEditImage);
        rvUserProfile = (RecyclerView) findViewById(R.id.xrvUserProfile);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rvUserProfile.setLayoutManager(layoutManager);

        mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        mFeedsDbRef = FirebaseDatabase.getInstance().getReference().child("feeds");
        mAuth = FirebaseAuth.getInstance();
        String current_uid = mAuth.getCurrentUser().getUid();
        mUserDbRef.keepSynced(true);
        mFeedsDbRef.keepSynced(true);

        userOnlyQuery = mFeedsDbRef.orderByChild("u_id").equalTo(user_id);

        if (current_uid.equals(user_id)) {
            ibEditImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent pickImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    pickImageIntent.setType("image/*");
                    startActivityForResult(pickImageIntent, PICK_IMAGE);
                }
            });
        } else {
            ibEditImage.setVisibility(View.GONE);
        }

        mUserDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                String image = dataSnapshot.child("profile_image").getValue(String.class);

                tvUserName.setText(name);
                tvUserEmail.setText(email);
                Glide.with(getApplicationContext())
                        .load(image)
                        .placeholder(R.mipmap.empty_user)
                        .crossFade()
                        .into(ivUserImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<Feed, MainActivity.FeedsViewHolder> userProfileFirebaseAdapter = new FirebaseRecyclerAdapter<Feed, MainActivity.FeedsViewHolder>(
                Feed.class,
                R.layout.single_row_main,
                MainActivity.FeedsViewHolder.class,
                userOnlyQuery
        ) {
            @Override
            protected void populateViewHolder(final MainActivity.FeedsViewHolder viewHolder, Feed model, int position) {

                final String feedU_id = model.getU_id();
                DatabaseReference dbUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(feedU_id);
                dbUserRef.keepSynced(true);
                dbUserRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userName = dataSnapshot.child("name").getValue(String.class);
                        String userProfileImage = dataSnapshot.child("profile_image").getValue(String.class);

                        viewHolder.setUserName(userName);
                        viewHolder.setUserImage(AnotherUserProfileActivity.this, userProfileImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setDate(model.getDate());
                viewHolder.setImage(getApplicationContext(), model.getImage());
            }
        };

        rvUserProfile.setAdapter(userProfileFirebaseAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                Glide.with(AnotherUserProfileActivity.this).load(resultUri).into(ivUserImage);

                StorageReference mProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile_images")
                        .child(mAuth.getCurrentUser().getUid());
                mProfileImageRef.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(AnotherUserProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        mUserDbRef.child("profile_image").setValue(downloadUrl.toString());

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
