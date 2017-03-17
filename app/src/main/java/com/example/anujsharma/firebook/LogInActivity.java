package com.example.anujsharma.firebook;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LogInActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button bnLogIn, bnNewAccount;
    private SignInButton bnGoogleSignIn;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseRef.keepSynced(true);

        etEmail = (EditText) findViewById(R.id.xetLogInEmail);
        etPassword = (EditText) findViewById(R.id.xetLogInPassword);
        bnLogIn = (Button) findViewById(R.id.xbnLogIn);
        bnNewAccount = (Button) findViewById(R.id.xbnLogInNewAccount);
        bnGoogleSignIn = (SignInButton) findViewById(R.id.xbnGoogleSignIn);
        mProgressDialog = new ProgressDialog(this);

        bnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogIn();
            }
        });

        bnNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

    }

    private void doLogIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!email.isEmpty() && !password.isEmpty()) {

            mProgressDialog.setMessage("Logging In");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        checkUserExists();
                    } else {
                        Toast.makeText(LogInActivity.this, "Enter correct email and password", Toast.LENGTH_SHORT).show();
                    }
                    mProgressDialog.dismiss();
                }
            });


        }

    }

    private void checkUserExists() {
        final String user_id = mFirebaseAuth.getCurrentUser().getUid();

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user_id)) {

                    mProgressDialog.dismiss();
                    Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Toast.makeText(LogInActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
