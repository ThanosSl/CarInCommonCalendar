package com.example.car_in_common_test2;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MapsActivity";

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    private HashMap<Marker, String[]> markerDataMap = new HashMap<>(); // Store marker details

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Firebase database reference and auth
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        } else {
            initializeMap();
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null; // Default frame
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate custom InfoWindow layout
                View view = LayoutInflater.from(MapsActivity.this).inflate(R.layout.info_window, null);

                TextView carModelText = view.findViewById(R.id.carModel);
                TextView carDetailsText = view.findViewById(R.id.carDetails);

                // Retrieve data for this marker
                String[] details = markerDataMap.get(marker);
                if (details != null) {
                    carModelText.setText("Team: " + details[0]); // Team Name
                    carDetailsText.setText("Model: " + details[1] + " | Plate: " + details[2]); // Car Model and Plate
                }

                return view;
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fetchAndShowUserLocation();
            loadCarDetailsFromFirebase();
        }
    }

    private void fetchAndShowUserLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(currentLocation)
                                .title("You are here!"));

                        if (marker != null) {
                            marker.showInfoWindow();
                        }
                    }
                });
    }

    private void loadCarDetailsFromFirebase() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMap.clear(); // Clear previous markers
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot carDetailsSnapshot = userSnapshot.child("carDetails");

                    // Retrieve car details
                    String teamName = carDetailsSnapshot.child("teamName").getValue(String.class);
                    String carModel = carDetailsSnapshot.child("carModel").getValue(String.class);
                    String carPlate = carDetailsSnapshot.child("carPlate").getValue(String.class);

                    // Mock coordinates (replace with actual data if available)
                    double latitude = 37.9838; // Replace with actual latitude
                    double longitude = 23.7275; // Replace with actual longitude

                    // Add marker to the map
                    if (teamName != null && carModel != null && carPlate != null) {
                        LatLng latLng = new LatLng(latitude, longitude);
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(teamName)); // Use team name as marker title

                        // Store marker data
                        if (marker != null) {
                            markerDataMap.put(marker, new String[]{teamName, carModel, carPlate});
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapsActivity.this, "Failed to load car details.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap();
            } else {
                Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
