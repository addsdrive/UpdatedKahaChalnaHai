package com.example.dirty.kahanchalnahai;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.CharacterPickerDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginRegisterActivity extends AppCompatActivity
{
    private Button  CustomerLogin , CustomerRegister ;
    private TextView CustomerRegisterLink , CustomerStatus ;
    private EditText CustomerEmail , CustomerPassword;
    private FirebaseAuth mAuth ;
    private ProgressDialog loadingBar ;
    private DatabaseReference CustomerDatabaseRef ;
    private String OnlineCustomerID ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);
        //Initialize the fields
        mAuth =   FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
        CustomerLogin = (Button)findViewById(R.id.customer_login);
        CustomerRegister = (Button)findViewById(R.id.customer_register);
        CustomerRegisterLink = (TextView)findViewById(R.id.customer_register_link);
        CustomerStatus = (TextView)findViewById(R.id.customer_status);
        CustomerEmail = (EditText)findViewById(R.id.customer_email);
        CustomerPassword = (EditText)findViewById(R.id.customer_password);
        CustomerRegister.setVisibility(View.INVISIBLE);
        CustomerRegister.setEnabled(false);
        //click listener for registering the customer
        CustomerRegisterLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CustomerLogin.setVisibility(View.INVISIBLE);
                CustomerRegisterLink.setVisibility(View.INVISIBLE);
                CustomerStatus.setText("Register Customer");
                CustomerRegister.setVisibility( View.VISIBLE);
                CustomerRegister.setEnabled(true);
            }
        });
        CustomerRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email = CustomerEmail.getText().toString();
                String password = CustomerPassword.getText().toString();
                RegisterCustomer(email , password);
            }
        });
        CustomerLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email = CustomerEmail.getText().toString();
                String password = CustomerPassword.getText().toString();
                LoginCustomer(email , password);
            }
        });
    }

    private void LoginCustomer(String email, String password)
    {
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this , "Enter your email " , Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this , "Enter your password " , Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Customer Login");
            loadingBar.setMessage("Please wait , we are checking your credientials as Customer");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.dismiss();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(CustomerLoginRegisterActivity.this, new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful())
                    {
                        Intent customerIntent = new Intent(CustomerLoginRegisterActivity.this , CustomerMapsActivity.class);
                        startActivity(customerIntent);
                        Toast.makeText(CustomerLoginRegisterActivity.this , "You are logged in successfully as Customer " , Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        Toast.makeText(CustomerLoginRegisterActivity.this , "Log In Unsuccessful ! Please try later " , Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void RegisterCustomer(String email, String password)
    {
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this , "Enter your email " , Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this , "Enter your password " , Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Customer Registration");
            loadingBar.setMessage("Please wait , we are registering you as Customer");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.dismiss();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(CustomerLoginRegisterActivity.this, new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful())
                    {
                        OnlineCustomerID = mAuth.getCurrentUser().getUid();
                        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(OnlineCustomerID);
                        CustomerDatabaseRef.setValue(true);
                        Intent driverIntent = new Intent(CustomerLoginRegisterActivity.this , CustomerMapsActivity.class);
                        startActivity(driverIntent);
                        Toast.makeText(CustomerLoginRegisterActivity.this , "You are registered successfully as Customer " , Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        String msg = task.getException().toString();
                        Toast.makeText(CustomerLoginRegisterActivity.this , "Registration Unsuccessful ! Please try later . Error :  "+msg , Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }
}
