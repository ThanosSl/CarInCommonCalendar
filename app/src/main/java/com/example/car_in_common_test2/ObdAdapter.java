    package com.example.car_in_common_test2;

    import java.net.Socket;

    public class ObdAdapter {
        private final String ip;
        private final int port;

        // Constructor to initialize IP and port
        public ObdAdapter(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        /**
         * Establishes a connection to the OBD-II adapter.
         * @return Socket connected to the OBD-II adapter, or null if the connection fails.
         */
        public Socket connectToObdAdapter() {
            try {
                Socket socket = new Socket(ip, port);
                System.out.println("Connected to OBD-II adapter at " + ip + ":" + port);
                return socket;
            } catch (Exception e) {
                System.err.println("Failed to connect to OBD-II adapter: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
    }