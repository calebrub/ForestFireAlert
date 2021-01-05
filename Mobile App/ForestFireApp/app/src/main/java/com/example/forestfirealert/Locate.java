package com.example.forestfirealert;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.helpers.FetchURL;
import com.example.helpers.TaskLoadedCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.regex.Pattern;

public class Locate extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback, LocationListener {

    private GoogleMap mMap;
    private MarkerOptions here,  sensor;
    LatLng l_here, l_sensor;
    Button getDirection;
    LocationManager locationManager;
    Location location;
    String l;

    private Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);

        RegisteredSensor registeredSensor = new RegisteredSensor();
        MediaPlayer mp = registeredSensor.mp;
        MediaPlayer smp = registeredSensor.smp;



        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        l = getIntent().getStringExtra("location");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }

        location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);

        mapFragment.getMapAsync(Locate.this);



       // l_sensor = new LatLng(0.332235, 32.570194);
        String[] ll = l.split(",");
        l_sensor = new LatLng(Double.parseDouble(ll[0]), Double.parseDouble(ll[1].replace(" ","")));
        sensor = new MarkerOptions().position(l_sensor).title("Sensor 1 Location");

        getDirection = (Button) findViewById(R.id.get_direction);


        getDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q="+ l.replace(" ","")+"&mode=w "));
                        intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);

//                try{
//                    new FetchURL(Locate.this).execute(getUrl(here.getPosition(), sensor.getPosition(), "driving"), "driving");
//                }catch (Exception e){
//                    Toast.makeText(getApplicationContext(), "Please Check Internet Connection", Toast.LENGTH_LONG).show();
//                }
            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("mylog", "Added Markers");
        onLocationChanged(location);
        Marker sensorMaker = mMap.addMarker(sensor);
        sensorMaker.showInfoWindow();
        float zoomLevel = 16.0f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(l_sensor, zoomLevel));
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;        l_here = new LatLng(location.getLatitude(), location.getLongitude());

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    @Override
    public void onLocationChanged(Location location) {
       try {
           l_here = new LatLng(location.getLatitude(), location.getLongitude());
       }catch (Exception e){
           Toast.makeText(this, "Turn Location on, and Check your Internet connection", Toast.LENGTH_LONG).show();
       }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}


