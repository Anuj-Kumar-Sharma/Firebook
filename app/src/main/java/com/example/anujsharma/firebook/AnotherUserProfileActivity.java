package com.example.anujsharma.firebook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class AnotherUserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 3;
    private ImageView ivUserImage;
    private TextView tvUserName, tvUserEmail;
    private ImageButton ibEditImage;

    private DatabaseReference mUserDbRef;
    private FirebaseAuth mAuth;
    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another_user_profile);

        Intent intent = getIntent();
        String user_id = intent.getStringExtra("feed_uid");

        ivUserImage = (ImageView) findViewById(R.id.xivAnotherUserImage);
        tvUserName = (TextView) findViewById(R.id.xtvAnotherUserName);
        tvUserEmail = (TextView) findViewById(R.id.xtvAnotherUserEmail);
        ibEditImage = (ImageButton) findViewById(R.id.xibEditImage);

        mUserDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        mAuth = FirebaseAuth.getInstance();
        String current_uid = mAuth.getCurrentUser().getUid();
        mUserDbRef.keepSynced(true);

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
