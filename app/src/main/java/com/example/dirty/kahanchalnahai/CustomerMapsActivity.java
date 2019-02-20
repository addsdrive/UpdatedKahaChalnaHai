package com.example.dirty.kahanchalnahai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;

import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.List;
import com.google.android.gms.location.FusedLocationProviderClient;

public class CustomerMapsActivity extends FragmentActivity implements
        OnMapReadyCallback {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    private Button CustomerLogOut , CustomerSettings , CustomerCallCab ;
    private FirebaseAuth mAuth ;
    private FirebaseUser currentUser ;
    private String CustomerID , DriverFoundID ;
    private int Radius = 1 ;
    private boolean DriverFound = false ;
    private LatLng CustomerPickUpLocation ;
    private DatabaseReference CustomerDatabaseRef , DriverAvailableRef , DriverRef , DriverLocationRef ;
    Marker DriverMarker ;
    FusedLocationProviderClient locationProviderClient;
    Marker user_marker;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        CustomerID = mAuth.getCurrentUser().getUid();
        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customer's Request");
        DriverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        DriverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
        CustomerLogOut = (Button)findViewById(R.id.customer_logout);
        CustomerSettings = (Button)findViewById(R.id.customer_settings);
        CustomerCallCab = (Button)findViewById(R.id.customer_call_cab);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        CustomerLogOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mAuth.signOut();
                LogOutCustomer();
            }
        });
                CustomerCallCab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {//Storing customer's request in firebase
                GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
                GeoLocation geoLocation = new GeoLocation(lastLocation.getLatitude() , lastLocation.getLongitude());
                geoFire.setLocation(CustomerID , geoLocation);
                CustomerPickUpLocation = new LatLng(lastLocation.getLatitude() , lastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(CustomerPickUpLocation).title("PickUp Customer From  Here"));
                CustomerCallCab.setText("Driver is on way to you ");
                GetClosestDriverCab();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(false);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (Permission.getInstance(CustomerMapsActivity.this).checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            locationProviderClient.requestLocationUpdates(locationRequest, mLocationCallBack, Looper.myLooper());
        }
    }



    LocationCallback mLocationCallBack = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            lastLocation = locationResult.getLastLocation();
            LatLng latLng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
            if(user_marker != null)
                user_marker.remove();
            user_marker = mMap.addMarker(new MarkerOptions().position(latLng).title("My Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        }
    };

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    private void LogOutCustomer()
    {
        Intent welcomeIntent = new Intent(CustomerMapsActivity.this , WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeIntent);
        finish();
    }

    private void GetClosestDriverCab()
    {
        GeoFire geoFire = new GeoFire(DriverAvailableRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(CustomerPickUpLocation.latitude , CustomerPickUpLocation.longitude) , Radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener()
        {
            @Override
            public void onKeyEntered(String key, GeoLocation location)
            {
                if (!DriverFound)
                {
                    DriverFound = true ;
                    DriverFoundID = key ;
                    DriverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DriverFoundID);
                    HashMap DriverMap = new HashMap();
                    DriverMap.put("customer_ride_id" ,CustomerID );
                    DriverRef.updateChildren(DriverMap);
                    GettingDriverLocation();
                    CustomerCallCab.setText("Looking For Driver's Location");
                }
            }

            @Override
            public void onKeyExited(String key)
            {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location)
            {

            }

            @Override
            public void onGeoQueryReady()
            {
                if (!DriverFound)
                {
                    Radius = Radius +1;
                    GetClosestDriverCab();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error)
            {

            }
        });
    }

    private void GettingDriverLocation()
    {
        DriverLocationRef.child(DriverFoundID).child("l").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    List<Object> DriverLocationMap = (List<Object>) dataSnapshot.getValue();
                    double LocationLat = 0;
                    double LocationLng = 0 ;
                    CustomerCallCab.setText("Driver Found");
                    if (DriverLocationMap.get(0) != null)
                    {
                        LocationLat = Double.parseDouble(DriverLocationMap.get(0).toString());
                    }
                    if (DriverLocationMap.get(1) != null)
                    {
                        LocationLng = Double.parseDouble(DriverLocationMap.get(1).toString());
                    }
                    LatLng DriverLatLng = new LatLng(LocationLat , LocationLng);
                    if (DriverMarker != null)
                    {
                        DriverMarker.remove();
                    }
                    //Distance between customer and driver
                    Location customerlocation = new Location("");
                    customerlocation.setLatitude(CustomerPickUpLocation.latitude);
                    customerlocation.setLongitude(CustomerPickUpLocation.longitude);
                    Location driverlocation = new Location("");
                    driverlocation.setLatitude(DriverLatLng.latitude);
                    driverlocation.setLongitude(DriverLatLng.longitude);
                    //Firebase in bult function
                    float Distance = customerlocation.distanceTo(driverlocation);
                    CustomerCallCab.setText("Driver Found" + String.valueOf(Distance));
                    DriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Your Driver is here"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            //LOCATION_PERMISSION_CODE = 101 inside Permission class
            case 101: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    onMapReady(mMap);
                } else {
                    // permission denied,
                    // Ask again for the permission
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
