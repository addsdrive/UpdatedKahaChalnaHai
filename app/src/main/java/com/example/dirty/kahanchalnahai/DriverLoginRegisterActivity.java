package com.example.dirty.kahanchalnahai;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.ref.SoftReference;

public class DriverLoginRegisterActivity extends AppCompatActivity
{
    private Button DriverLogin , DriverRegister ;
    private TextView DriverRegisterLink , DriverStatus ;
    private EditText DriverEmail , DriverPassword;
    private FirebaseAuth mAuth ;
    private ProgressDialog loadingBar ;
    private DatabaseReference DriverDatabaseRef ;
    private String OnlineDriverID ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);
        //Initialize the fields
        mAuth =   FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
        DriverLogin = (Button)findViewById(R.id.driver_login);
        DriverRegister = (Button)findViewById(R.id.driver_register);
        DriverRegisterLink = (TextView)findViewById(R.id.driver_register_link);
        DriverStatus = (TextView)findViewById(R.id.driver_status);
        DriverEmail = (EditText)findViewById(R.id.driver_email);
        DriverPassword = (EditText)findViewById(R.id.driver_password);
        DriverRegister.setVisibility(View.INVISIBLE);
        DriverRegister.setEnabled(false);
        //click listener for registering the customer
        DriverRegisterLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DriverLogin.setVisibility(View.INVISIBLE);
                DriverRegisterLink.setVisibility(View.INVISIBLE);
                DriverStatus.setText("Register Driver");
                DriverRegister.setVisibility( View.VISIBLE);
                DriverRegister.setEnabled(true);
            }
        });
        DriverRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String registerEmail = DriverEmail.getText().toString();
                String registerPassword = DriverPassword.getText().toString();
                RegisterDriver(registerEmail , registerPassword);
            }
        });
        DriverLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String loginEmail = DriverEmail.getText().toString();
                String loginPassword = DriverPassword.getText().toString();
                LoginDriver(loginEmail , loginPassword);
            }
        });
    }

    private void LoginDriver(String loginEmail, String loginPassword)
    {
        if (TextUtils.isEmpty(loginEmail))
        {
            Toast.makeText(DriverLoginRegisterActivity.this , "Enter your email " , Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(loginPassword))
        {
            Toast.makeText(DriverLoginRegisterActivity.this , "Enter your password " , Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("'Driver Login ");
            loadingBar.setMessage("Please wait , we are checking your credientials you as Driver");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.dismiss();
            mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(DriverLoginRegisterActivity.this, new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful())
                    {
                        //Send user to maps activity
                        Intent driverIntent = new Intent(DriverLoginRegisterActivity.this , DriversMapActivity.class);
                        startActivity(driverIntent);
                        Toast.makeText(DriverLoginRegisterActivity.this , "You are logged in successfully as Driver " , Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        Toast.makeText(DriverLoginRegisterActivity.this , "Log In Unsuccessful ! Please try later " , Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void RegisterDriver(String registerEmail, String registerPassword)
    {
        if (TextUtils.isEmpty(registerEmail))
        {
            Toast.makeText(DriverLoginRegisterActivity.this , "Enter your email " , Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(registerPassword))
        {
            Toast.makeText(DriverLoginRegisterActivity.this , "Enter your password " , Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Driver Registration");
            loadingBar.setMessage("Please wait , we are registering you as Driver");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.dismiss();
            mAuth.createUserWithEmailAndPassword(registerEmail, registerPassword).addOnCompleteListener(DriverLoginRegisterActivity.this, new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful())
                    {
                        OnlineDriverID = mAuth.getCurrentUser().getUid();
                        DriverDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(OnlineDriverID);
                        DriverDatabaseRef.setValue(true);
                        Intent driverIntent = new Intent(DriverLoginRegisterActivity.this , DriversMapActivity.class);
                        startActivity(driverIntent);
                        Toast.makeText(DriverLoginRegisterActivity.this , "You are registered successfully as Driver " , Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        Toast.makeText(DriverLoginRegisterActivity.this , "Registration Unsuccessful ! Please try later " , Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }
}
