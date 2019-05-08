package com.example.magicman.googleauthentication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity
{
    static final String TAG= "MainActivity";
    static final int RC_SIGN_IN=0;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    com.google.android.gms.common.SignInButton signInButton;
    GoogleApiClient mGoogleApiClient;
    Button signOutButton;
    TextView nameTextView,emailTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameTextView= (TextView) findViewById(R.id.textView);
        emailTextView= (TextView) findViewById(R.id.textView2);
        signInButton= (SignInButton) findViewById(R.id.signInButton);
        signOutButton= (Button) findViewById(R.id.signOutButton);

        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        mGoogleApiClient=new GoogleApiClient.Builder(this).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
            {

            }
        }).addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();

        mAuth=FirebaseAuth.getInstance();

        mAuthListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                signInButton.setVisibility(View.GONE);
                signOutButton.setVisibility(View.VISIBLE);

                if(user!=null)
                {
                    if(user.getDisplayName() !=null)
                    {
                        nameTextView.setText("HI "+user.getDisplayName().toString());
                        emailTextView.setText(user.getEmail().toString());
                    }else
                    {

                    }
                }

            }
        };

        signInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signIn();

            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FirebaseAuth.getInstance().signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>()
                {
                    @Override
                    public void onResult(@NonNull Status status)
                    {
                        signInButton.setVisibility(View.VISIBLE);
                        signOutButton.setVisibility(View.GONE);
                        emailTextView.setText("");
                        nameTextView.setText("");
                    }
                });

            }
        });
    }
     private void  signIn()
     {
         Intent signInIntent=Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
         startActivityForResult(signInIntent,RC_SIGN_IN);

     }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode==RC_SIGN_IN)
        {
            GoogleSignInResult result=Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess())
            {
                //Google Sign In was Successful,
                //Authenticate With Firebase.
                GoogleSignInAccount account=result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
            else
            {
                //Google Sign In failed
            }
        }
        super.onActivityResult(requestCode,resultCode,data);

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(mAuthListener !=null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }
    private  void firebaseAuthWithGoogle(GoogleSignInAccount acct)
    {
        AuthCredential credential= GoogleAuthProvider.getCredential(acct.getIdToken(),null);


        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(!task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this,"Authentication Failed",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}
