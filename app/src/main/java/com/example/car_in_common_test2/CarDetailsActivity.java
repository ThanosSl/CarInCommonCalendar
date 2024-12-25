package com.example.car_in_common_test2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class CarDetailsActivity extends AppCompatActivity {

    private EditText teamNameEditText, carModelEditText, carPlateEditText;
    private Button submitButton, chooseExistingCarButton;

    private DatabaseReference mCarDatabase, mUserDatabase;
    private ArrayList<String> carList;
    private HashMap<String, String> carIdMap; // Map to store car display name -> carId

    private String currentUserId, selectedCarId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details);

        // Initialize Firebase references
        mCarDatabase = FirebaseDatabase.getInstance().getReference("cars");
        mUserDatabase = FirebaseDatabase.getInstance().getReference("users");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize UI components
        teamNameEditText = findViewById(R.id.editTextTeamName);
        carModelEditText = findViewById(R.id.editTextCarModel);
        carPlateEditText = findViewById(R.id.editTextCarPlate);
        submitButton = findViewById(R.id.buttonSubmitCarDetails);
        chooseExistingCarButton = findViewById(R.id.buttonChooseExistingCar);

        carList = new ArrayList<>();
        carIdMap = new HashMap<>();
        selectedCarId = null; // Initially null

        // Button listeners
        submitButton.setOnClickListener(v -> saveCarDetails());
        chooseExistingCarButton.setOnClickListener(v -> showCarSelectionDialog());
    }

    private void saveCarDetails() {
        String teamName = teamNameEditText.getText().toString().trim();
        String carModel = carModelEditText.getText().toString().trim();
        String carPlate = carPlateEditText.getText().toString().trim();

        if (teamName.isEmpty() || carModel.isEmpty() || carPlate.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCarId != null) {
            // If an existing car is selected, link it to the user
            linkCarToUser(selectedCarId);
        } else {
            // Check for existing car before saving
            mCarDatabase.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String existingCarId = null;

                    // Check if a car with the same details exists
                    for (DataSnapshot snapshot : task.getResult().getChildren()) {
                        CarDetails car = snapshot.getValue(CarDetails.class);
                        if (car != null && car.teamName.equals(teamName) &&
                                car.carModel.equals(carModel) && car.carPlate.equals(carPlate)) {
                            existingCarId = snapshot.getKey();
                            break;
                        }
                    }

                    if (existingCarId != null) {
                        // Car already exists, link it to the user
                        linkCarToUser(existingCarId);
                        Toast.makeText(this, "Car already exists. Linked to your account.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Save new car data
                        String carId = mCarDatabase.push().getKey();
                        if (carId != null) {
                            CarDetails carDetails = new CarDetails(teamName, carModel, carPlate);
                            mCarDatabase.child(carId).setValue(carDetails)
                                    .addOnSuccessListener(aVoid -> {
                                        linkCarToUser(carId);
                                        Toast.makeText(this, "Car details saved.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to save car details.", Toast.LENGTH_SHORT).show());
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to check existing cars.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void linkCarToUser(String carId) {
        // Link the car ID to the current user
        mUserDatabase.child(currentUserId).child("selectedCarId").setValue(carId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Car assigned successfully.", Toast.LENGTH_SHORT).show();
                    // Navigate to MainMenuActivity
                    Intent intent = new Intent(CarDetailsActivity.this, MainMenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to assign car to user.", Toast.LENGTH_SHORT).show());
    }

    private void showCarSelectionDialog() {
        mCarDatabase.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                carList.clear();
                carIdMap.clear();

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String carId = snapshot.getKey(); // Get the existing car ID
                    CarDetails car = snapshot.getValue(CarDetails.class);

                    if (car != null) {
                        String displayName = car.teamName + " - " + car.carModel + " - " + car.carPlate;
                        carList.add(displayName);
                        carIdMap.put(displayName, carId); // Map display name to car ID
                    }
                }

                if (carList.isEmpty()) {
                    Toast.makeText(this, "No cars available.", Toast.LENGTH_SHORT).show();
                } else {
                    showCarDialog();
                }
            } else {
                Toast.makeText(this, "Failed to fetch car details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCarDialog() {
        String[] carArray = carList.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a Car");
        builder.setItems(carArray, (dialog, which) -> {
            String selectedCar = carArray[which];
            selectedCarId = carIdMap.get(selectedCar); // Retrieve the car ID

            if (selectedCarId != null) {
                // Fetch car details and populate the fields
                mCarDatabase.child(selectedCarId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        CarDetails car = task.getResult().getValue(CarDetails.class);
                        if (car != null) {
                            teamNameEditText.setText(car.teamName);
                            carModelEditText.setText(car.carModel);
                            carPlateEditText.setText(car.carPlate);
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch car details.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.show();
    }

    // CarDetails class to hold car data
    public static class CarDetails {
        public String teamName;
        public String carModel;
        public String carPlate;

        public CarDetails() {} // Default constructor required for Firebase

        public CarDetails(String teamName, String carModel, String carPlate) {
            this.teamName = teamName;
            this.carModel = carModel;
            this.carPlate = carPlate;
        }
    }
}
