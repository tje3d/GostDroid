package net.typeblog.socks;

import android.util.Log;

import gostlib.Gostlib;

/**
 * Wrapper class for the Gost mobile library
 * Provides an interface to start and stop Gost tunnels
 */
public class GostTunnel {
    private static final String TAG = "GostTunnel";
    private static boolean isRunning = false;

    /**
     * Start a Gost tunnel with the specified transport and server address
     * @param transport The transport protocol (ws, wss, ssh, etc.)
     * @param serverAddr The server address (e.g., "example.com:8080")
     * @return true if tunnel started successfully, false otherwise
     */
    public static boolean startTunnel(String transport, String serverAddr) {
        return startTunnel(transport, serverAddr, "", "");
    }

    /**
     * Start a Gost tunnel with the specified transport, server address, and authentication
     * @param transport The transport protocol (ws, wss, ssh, etc.)
     * @param serverAddr The server address (e.g., "example.com:8080")
     * @param username The username for authentication (empty string if not needed)
     * @param password The password for authentication (empty string if not needed)
     * @return true if tunnel started successfully, false otherwise
     */
    public static boolean startTunnel(String transport, String serverAddr, String username, String password) {
        try {
            if (isRunning) {
                Log.w(TAG, "Tunnel is already running");
                return true;
            }

            Log.i(TAG, "Starting Gost tunnel with transport: " + transport + ", server: " + serverAddr + 
                  (username.isEmpty() ? "" : ", username: " + username));
            Gostlib.startTunnel(transport, serverAddr, username, password);
            isRunning = true;
            Log.i(TAG, "Gost tunnel started successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to start Gost tunnel", e);
            isRunning = false;
            return false;
        }
    }

    /**
     * Stop the currently running Gost tunnel
     * @return true if tunnel stopped successfully, false otherwise
     */
    public static boolean stopTunnel() {
        try {
            if (!isRunning) {
                Log.w(TAG, "No tunnel is currently running");
                return true;
            }

            Log.i(TAG, "Stopping Gost tunnel");
            Gostlib.stopTunnel();
            isRunning = false;
            Log.i(TAG, "Gost tunnel stopped successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop Gost tunnel", e);
            return false;
        }
    }

    /**
     * Check if the tunnel is currently running
     * @return true if tunnel is running, false otherwise
     */
    public static boolean isRunning() {
        return isRunning;
    }
}