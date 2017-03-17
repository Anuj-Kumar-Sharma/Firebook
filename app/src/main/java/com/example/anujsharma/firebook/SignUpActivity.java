package com.example.anujsharma.firebook;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText etSignUpName, etSignUpEmail, etSignUpPassword;
    private Button bnSignUp;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");

        mProgressDialog = new ProgressDialog(this);

        etSignUpName = (EditText) findViewById(R.id.xetSignUpName);
        etSignUpEmail = (EditText) findViewById(R.id.xetSignUpEmail);
        etSignUpPassword = (EditText) findViewById(R.id.xetSignUpPassword);
        bnSignUp = (Button) findViewById(R.id.xbnSignUp);

        bnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSignUp();
            }
        });
    }

    private void doSignUp() {
        final String name = etSignUpName.getText().toString().trim();
        final String email = etSignUpEmail.getText().toString().trim();
        String pass = etSignUpPassword.getText().toString().trim();

        if (!name.isEmpty() && !email.isEmpty() && !pass.isEmpty()) {

            mProgressDialog.setMessage("Signing Up");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            mFirebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    String user_id = mFirebaseAuth.getCurrentUser().getUid();

                    DatabaseReference userDbRef = mDatabaseRef.child(user_id);
                    userDbRef.child("name").setValue(name);
                    userDbRef.child("email").setValue(email);

                    mProgressDialog.dismiss();
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
        }
    }
}
