package com.example.bosschuse.pingtaxi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.constant.TransitMode;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends AppCompatActivity {
    Button btn_Done;
    EditText edt_Dropoff;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    PlaceAutocompleteFragment placeAutocompleteFragment;
    MapFragment myMap;
    Marker myMarker;
    LatLng current;
    LatLngBounds destination;
    double current_Lat,current_Lng;
    int PLACE_PICKER_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_Done = (Button) findViewById(R.id.btnDone);
        edt_Dropoff = (EditText) findViewById(R.id.edtDropoff);

        edt_Dropoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    startActivityForResult(builder.build(MainActivity.this), PLACE_PICKER_REQUEST);
                }
                catch (GooglePlayServicesRepairableException e){
                    Err_AlertDialog("GooglePlayServicesRepairableException"+e.getMessage());
                }
                catch (GooglePlayServicesNotAvailableException e){
                    Err_AlertDialog("GooglePlayServicesNotAvailableException : "+e.getMessage());
                }
            }
        });
        googleApiClient = new GoogleApiClient
                .Builder(MainActivity.this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        btn_Done.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(current != null && !edt_Dropoff.getText().toString().equals("") && destination != null){
                                    GoogleDirection.withServerKey("AIzaSyBgdSDi0D4v93Yfuv0M3yWrqkEYzdgWZf8")
                                            .from(current)
                                            .to(destination.getCenter())
                                            .transportMode(TransportMode.DRIVING)
                                            .transitMode(TransitMode.BUS)
                                            .unit(Unit.METRIC)
                                            .avoid(AvoidType.FERRIES)
                                            .alternativeRoute(true)
                                            .execute(new DirectionCallback() {
                                                @Override
                                                public void onDirectionSuccess(Direction direction, String rawBody) {
                                                    Err_AlertDialog("Create Diraction Success");
                                                }

                                                @Override
                                                public void onDirectionFailure(Throwable t) {
                                                    Err_AlertDialog("Create Diraction Fail");
                                                }
                                            });
                                }
                            }
                        });
                        locationRequest = LocationRequest.create()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10 * 10000)
                                .setFastestInterval(1 * 1000);
                        final Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                        myMap = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                        myMap.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(final GoogleMap googleMap) {
                                googleMap.setMyLocationEnabled(true);
                                googleMap.setTrafficEnabled(true);
                                try {
                                    current_Lat = location.getLatitude();
                                    current_Lng = location.getLongitude();
                                    current = new LatLng(current_Lat, current_Lng);
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 14));

                                }
                                catch (NullPointerException e)
                                {
                                    Err_AlertDialog("No Location Found Please Turn On GPS");
                                }
                                catch (Exception e)
                                {
                                    Err_AlertDialog("Unknow Error" +
                                            "\nError Message : "+e.getMessage());
                                }
                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Toast.makeText(MainActivity.this, "googleApiClinet Error", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
    }
    @Override
    public void onStart(){
        super.onStart();
        googleApiClient.connect();
    }
    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }
    private void Err_AlertDialog(String text){
        AlertDialog err = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Error !")
                .setMessage(text)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("%s", place.getName());
                edt_Dropoff.setText(toastMsg);
                destination = PlacePicker.getLatLngBounds(data);
            }
        }
    }
}