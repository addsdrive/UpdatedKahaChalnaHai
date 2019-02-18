package com.example.dirty.kahanchalnahai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
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

public class CustomerMapsActivity extends FragmentActivity implements
        OnMapReadyCallback , GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener
{

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient =  new GoogleApiClient.Builder(this).addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        locationRequest = new LocationRequest();
        //To update the last location of user after every second
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
    }

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

}
