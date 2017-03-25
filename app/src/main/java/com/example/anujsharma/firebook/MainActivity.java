package com.example.anujsharma.firebook;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dataStructures.Feed;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab;
    private RecyclerView rvMainRecyclerView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView navigationView;
    private TextView navigationUserName, navigationUserEmail;
    private CircleImageView navigationUserImage;
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


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PostActivity.class));
            }
        });


        mDrawerLayout = (DrawerLayout) findViewById(R.id.mainDrawerLayout);
        navigationView = (NavigationView) findViewById(R.id.mainNavigationView);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                fab.setAlpha(1 - slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                fab.setVisibility(View.GONE);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                fab.setVisibility(View.VISIBLE);
            }
        };
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        View mview = navigationView.getHeaderView(0);
        navigationUserImage = (CircleImageView) mview.findViewById(R.id.xcivUserProfileImage);
        navigationUserName = (TextView) mview.findViewById(R.id.xtvUserNameInNavigation);
        navigationUserEmail = (TextView) mview.findViewById(R.id.xtvUserEmailInNavigation);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("feeds");
        mDatabaseUsersRef = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseRef.keepSynced(true);
        mDatabaseUsersRef.keepSynced(true);

        rvMainRecyclerView = (RecyclerView) findViewById(R.id.xrvMainRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rvMainRecyclerView.setLayoutManager(layoutManager);

        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {
                    Intent intent = new Intent(MainActivity.this, LogInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        };

        mDatabaseUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String name = dataSnapshot.child(currentUser.getUid()).child("name").getValue(String.class);
                    String email = dataSnapshot.child(currentUser.getUid()).child("email").getValue(String.class);
                    String user_image = dataSnapshot.child(currentUser.getUid()).child("profile_image").getValue(String.class);

                    navigationUserName.setText(name);
                    navigationUserEmail.setText(email);
                    Glide.with(getApplicationContext())
                            .load(user_image)
                            .dontAnimate()
                            .into(navigationUserImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.nav_profile:
                        startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
                        break;
                    case R.id.nav_settings:
                        Toast.makeText(MainActivity.this, "settings", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_logout:

                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Log Out");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAuth.signOut();
                                Intent intent = new Intent(MainActivity.this, LogInActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                }
                return true;
            }
        });
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
            protected void populateViewHolder(final FeedsViewHolder viewHolder, final Feed model, int position) {

                final String feedU_id = model.getU_id();
                DatabaseReference dbUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(feedU_id);
                dbUserRef.keepSynced(true);
                dbUserRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userName = dataSnapshot.child("name").getValue(String.class);
                        String userProfileImage = dataSnapshot.child("profile_image").getValue(String.class);

                        viewHolder.setUserName(userName);
                        viewHolder.setUserImage(MainActivity.this, userProfileImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setDate(model.getDate());
                viewHolder.setImage(getApplicationContext(), model.getImage());

                viewHolder.tvUserProfileName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, AnotherUserProfileActivity.class);
                        intent.putExtra("feed_uid", feedU_id);
                        startActivity(intent);
                    }
                });

                viewHolder.ivUserImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, AnotherUserProfileActivity.class);
                        intent.putExtra("feed_uid", feedU_id);
                        startActivity(intent);
                    }
                });

                viewHolder.ivImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, ImageViewActivity.class);
                        intent.putExtra("image_uri", model.getImage());
                        startActivity(intent);
                    }
                });
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
        if (id == R.id.action_search) {
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
        }

        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class FeedsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView tvUserProfileName;
        CircleImageView ivUserImage;
        ImageView ivImage;

        public FeedsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            ivUserImage = (CircleImageView) mView.findViewById(R.id.xivUserImageInFeeds);
            tvUserProfileName = (TextView) mView.findViewById(R.id.xtvUserNameInFeeds);
            ivImage = (ImageView) mView.findViewById(R.id.xivImage);
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
            Glide.with(context).load(image).into(ivImage);
        }

        public void setUserImage(Context context, String userImage) {
            Glide.with(context)
                    .load(userImage)
                    .placeholder(R.mipmap.empty_user)
                    .crossFade()
                    .dontAnimate()
                    .into(ivUserImage);
        }

        public void setUserName(String userProfileName) {
            tvUserProfileName.setText(userProfileName);
        }

        public void setDate(String date) {
            TextView tvDate = (TextView) mView.findViewById(R.id.xtvDate);
            tvDate.setText(date);
        }
    }
}
