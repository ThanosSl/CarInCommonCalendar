package com.example.car_in_common_test2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.pires.obd.commands.ObdCommand;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ObdActivity extends AppCompatActivity {

    public interface FuelLevelListener {
        void onFuelLevelReceived(String fuelLevel);
        void onError(String errorMessage);
    }

    private Socket socket;

    public void fetchFuelLevel(FuelLevelListener listener) {
        new Thread(() -> {
            try {
                ObdAdapter obdAdapter = new ObdAdapter("192.168.0.10", 35000);
                socket = obdAdapter.connectToObdAdapter();

                if (socket == null) {
                    listener.onError("No OBD-II adapter connected.");
                    return;
                }

                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();

                // Enable headers
                outputStream.write("ATH1\r".getBytes());
                outputStream.flush();
                Thread.sleep(200);

                // Set the correct header for the PID
                outputStream.write("ATSH7C0\r".getBytes());
                outputStream.flush();
                Thread.sleep(200);

                // Execute the custom OBD command
                CustomFuelLevelCommand fuelLevelCommand = new CustomFuelLevelCommand();
                fuelLevelCommand.run(inputStream, outputStream);

                // Pass the fuel level back via the listener
                listener.onFuelLevelReceived(fuelLevelCommand.getFormattedResult());
            } catch (Exception e) {
                listener.onError("Error: " + e.getMessage());
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static class CustomFuelLevelCommand extends ObdCommand {

        private double fuelLevelPercentage = 0.0;

        public CustomFuelLevelCommand() {
            super("2129");
        }

        @Override
        protected void performCalculations() {
            String rawResult = getResult();
            if (rawResult != null && rawResult.length() >= 6) {
                try {
                    String hexValue = rawResult.substring(rawResult.length() - 2).trim();
                    int decimalValue = Integer.parseInt(hexValue, 16);
                    fuelLevelPercentage = (decimalValue / 255.0) * 100;
                } catch (NumberFormatException e) {
                    fuelLevelPercentage = 0.0;
                }
            } else {
                fuelLevelPercentage = 0.0;
            }
        }

        @Override
        public String getFormattedResult() {
            return String.format("%.2f%%", fuelLevelPercentage);
        }

        @Override
        public String getCalculatedResult() {
            return String.valueOf(fuelLevelPercentage);
        }

        @Override
        public String getName() {
            return "Custom Fuel Level Command";
        }
    }
}
