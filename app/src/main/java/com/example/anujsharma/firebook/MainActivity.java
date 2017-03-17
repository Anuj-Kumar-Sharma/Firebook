package com.example.anujsharma.firebook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dataStructures.Feed;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvMainRecyclerView;

    private DatabaseReference mDatabaseRef;
    private DatabaseReference mDatabaseUsersRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("feeds");
        mDatabaseUsersRef = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseRef.keepSynced(true);
        mDatabaseUsersRef.keepSynced(true);

        rvMainRecyclerView = (RecyclerView) findViewById(R.id.xrvMainRecyclerView);
        rvMainRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {
                    Intent intent = new Intent(MainActivity.this, LogInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Feed, FeedsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Feed, FeedsViewHolder>(

                Feed.class,
                R.layout.single_row_main,
                FeedsViewHolder.class,
                mDatabaseRef
        ) {
            @Override
            protected void populateViewHolder(FeedsViewHolder viewHolder, Feed model, int position) {

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(MainActivity.this, model.getImage());
            }
        };

        rvMainRecyclerView.setAdapter(firebaseRecyclerAdapter);

        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_add) {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        } else if (id == R.id.action_logout) {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else if (id == R.id.action_view_profile) {
            startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    public static class FeedsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FeedsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title) {
            TextView tvTitle = (TextView) mView.findViewById(R.id.xtvTitle);
            tvTitle.setText(title);
        }

        public void setDesc(String desc) {
            TextView tvDesc = (TextView) mView.findViewById(R.id.xtvDesc);
            tvDesc.setText(desc);
        }

        public void setImage(Context context, String image) {
            ImageView ivImage = (ImageView) mView.findViewById(R.id.xivImage);
            Glide.with(context).load(image).into(ivImage);
        }
    }
}
