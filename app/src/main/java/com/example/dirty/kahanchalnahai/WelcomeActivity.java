package com.example.dirty.kahanchalnahai;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity
{
    private Button WelcomeDriver , WelcomeCustomer ;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        WelcomeCustomer = (Button)findViewById(R.id.welcome_customer);
        WelcomeDriver = (Button)findViewById(R.id.welcome_driver);
        WelcomeCustomer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent customerIntent = new Intent(WelcomeActivity.this , CustomerLoginRegisterActivity.class);
                startActivity(customerIntent);
            }
        });
        WelcomeDriver.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent driverIntent = new Intent(WelcomeActivity.this , DriverLoginRegisterActivity.class);
                startActivity(driverIntent);
            }
        });
    }
}
