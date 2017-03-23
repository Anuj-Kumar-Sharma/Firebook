package com.example.anujsharma.firebook;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;

public class PostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 2;
    ImageButton ibImage;
    Button bnPost;
    EditText etTitle, etDesc;
    Uri resultUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private ProgressDialog mProgressDialog;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        ibImage = (ImageButton) findViewById(R.id.xibImage);
        bnPost = (Button) findViewById(R.id.xbnPost);
        etTitle = (EditText) findViewById(R.id.xetTitle);
        etDesc = (EditText) findViewById(R.id.xetDesc);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("feeds");
        mProgressDialog = new ProgressDialog(this);

        ibImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickImageIntent.setType("image/*");
                startActivityForResult(pickImageIntent, PICK_IMAGE);
            }
        });

        bnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postToFirebase();
            }
        });
    }

    private void postToFirebase() {

        final String title = etTitle.getText().toString().trim();
        final String desc = etDesc.getText().toString().trim();
        mProgressDialog.setTitle("Posting...");
        mProgressDialog.setCanceledOnTouchOutside(false);

        if(!title.isEmpty() && !desc.isEmpty() && resultUri!=null) {
            mProgressDialog.show();
            StorageReference imageRef = mStorageRef.child("firebook_feeeds").child(resultUri.getLastPathSegment());
            imageRef.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    Toast.makeText(PostActivity.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();

                    DatabaseReference mchildRef = mDatabaseRef.push();
                    mchildRef.child("title").setValue(title);
                    mchildRef.child("desc").setValue(desc);
                    mchildRef.child("u_id").setValue(mUser.getUid());
                    Calendar c = Calendar.getInstance();
                    String date = c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE) + (c.get(Calendar.AM_PM) == 1 ? " PM" : " AM") + "   "
                            + c.get(Calendar.DATE) + "/" + c.get(Calendar.MONTH) + "/" + c.get(Calendar.YEAR);

                    mchildRef.child("date").setValue(date);
                    mchildRef.child("image").setValue(downloadUrl.toString());

                    mProgressDialog.dismiss();

                    Intent intent = new Intent(PostActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mProgressDialog.dismiss();
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    mProgressDialog.setMessage((int)progress+"% uploaded...");
                }
            });

        }
        else {
            Toast.makeText(this, "Fill both title and description, also add some image.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK) {
            Uri imageUri= data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(16, 9)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                ibImage.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
