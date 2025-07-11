package net.typeblog.socks;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.typeblog.socks.util.Profile;
import net.typeblog.socks.util.ProfileManager;
import net.typeblog.socks.util.Utility;

public class VpnMainActivity extends Activity implements View.OnClickListener {
    private ProfileManager mManager;
    private Profile mProfile;
    
    private Button mBtnConnect;
    private TextView mServerAddress;
    private TextView mGostTransportInfo;
    private TextView mConnectionStatus;
    private LinearLayout mBtnSettings;
    private LinearLayout mBtnProfiles;
    private LinearLayout mGostInfo;
    
    private boolean mRunning = false;
    private boolean mStarting = false, mStopping = false;
    
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mBinder = IVpnService.Stub.asInterface(binder);
            
            try {
                mRunning = mBinder.isRunning();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            updateUI();
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
        }
    };
    
    private final Runnable mStateRunnable = new Runnable() {
        @Override
        public void run() {
            updateState();
            mBtnConnect.postDelayed(this, 1000);
        }
    };
    
    private IVpnService mBinder;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup transparent status bar
        setupTransparentStatusBar();
        
        setContentView(R.layout.activity_main);
        
        // Hide the action bar/title
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        
        mManager = new ProfileManager(getApplicationContext());
        mProfile = mManager.getDefault();
        
        initViews();
        updateServerInfo();
        updateGostInfo();
        checkState();
        
        // Start the state update loop
        mBtnConnect.postDelayed(mStateRunnable, 1000);
    }
    
    private void setupTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
            
            // Make the status bar content dark on light backgrounds, light on dark backgrounds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                // Since we have a dark theme, we want light status bar content
                decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
            } else {
                // For older versions, just use the layout flags
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBinder != null) {
            unbindService(mConnection);
        }
        mBtnConnect.removeCallbacks(mStateRunnable);
    }
    
    private void initViews() {
        mBtnConnect = findViewById(R.id.btn_connect);
        mServerAddress = findViewById(R.id.server_address);
        mGostTransportInfo = findViewById(R.id.gost_transport_info);
        mConnectionStatus = findViewById(R.id.connection_status);
        mBtnSettings = findViewById(R.id.btn_settings);
        mBtnProfiles = findViewById(R.id.btn_profiles);
        mGostInfo = findViewById(R.id.gost_info);
        
        mBtnConnect.setOnClickListener(this);
        mBtnSettings.setOnClickListener(this);
        mBtnProfiles.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v) {
        if (v == mBtnConnect) {
            if (mRunning) {
                stopVpn();
            } else {
                startVpn();
            }
        } else if (v == mBtnSettings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (v == mBtnProfiles) {
            Intent intent = new Intent(this, ProfilesActivity.class);
            startActivity(intent);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK) {
            Utility.startVpn(this, mProfile);
            checkState();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh profile in case it was changed in settings
        mProfile = mManager.getDefault();
        updateServerInfo();
        updateGostInfo();
    }
    
    private void updateServerInfo() {
        if (mProfile != null && mProfile.getServer() != null && !mProfile.getServer().isEmpty()) {
            String serverInfo = mProfile.getServer() + ":" + mProfile.getPort();
            mServerAddress.setText(serverInfo);
        } else {
            mServerAddress.setText(getString(R.string.not_configured));
        }
    }
    
    private void updateGostInfo() {
        if (mProfile != null && mProfile.useGost()) {
            mGostInfo.setVisibility(View.VISIBLE);
            String transport = mProfile.getGostTransport();
            String gostServer = mProfile.getGostServer();
            
            String displayTransport = "";
            if (transport != null && !transport.isEmpty()) {
                // Capitalize first letter and make it more readable
                displayTransport = transport.substring(0, 1).toUpperCase() + transport.substring(1);
                if (transport.equals("ws")) {
                    displayTransport = "WebSocket";
                } else if (transport.equals("wss")) {
                    displayTransport = "WebSocket Secure";
                } else if (transport.equals("h2")) {
                    displayTransport = "HTTP/2";
                }
            } else {
                displayTransport = "WebSocket";
            }
            
            // Include gost server address if available
            if (gostServer != null && !gostServer.trim().isEmpty()) {
                mGostTransportInfo.setText(displayTransport + "\n" + gostServer);
            } else {
                mGostTransportInfo.setText(displayTransport);
            }
        } else {
            mGostInfo.setVisibility(View.GONE);
        }
    }
    
    private void checkState() {
        mRunning = false;
        mBtnConnect.setEnabled(false);
        
        if (mBinder == null) {
            bindService(new Intent(this, SocksVpnService.class), mConnection, 0);
        }
    }
    
    private void updateState() {
        if (mBinder == null) {
            mRunning = false;
        } else {
            try {
                mRunning = mBinder.isRunning();
            } catch (Exception e) {
                mRunning = false;
            }
        }
        
        updateUI();
    }
    
    private void updateUI() {
        // Update connection status text
        if (mConnectionStatus != null) {
            if (mStarting) {
                mConnectionStatus.setText("Connecting...");
                mConnectionStatus.setTextColor(getResources().getColor(R.color.status_connecting));
            } else if (mStopping) {
                mConnectionStatus.setText("Disconnecting...");
                mConnectionStatus.setTextColor(getResources().getColor(R.color.status_connecting));
            } else if (mRunning) {
                mConnectionStatus.setText("Connected");
                mConnectionStatus.setTextColor(getResources().getColor(R.color.status_connected));
            } else {
                mConnectionStatus.setText("Disconnected");
                mConnectionStatus.setTextColor(getResources().getColor(R.color.status_disconnected));
            }
        }
        
        // Update button
        if (mStarting) {
            mBtnConnect.setText(getString(R.string.status_connecting));
            mBtnConnect.setEnabled(false);
        } else if (mStopping) {
            mBtnConnect.setText(getString(R.string.status_disconnecting));
            mBtnConnect.setEnabled(false);
        } else if (mRunning) {
            mBtnConnect.setText(getString(R.string.btn_disconnect));
            mBtnConnect.setEnabled(true);
            mBtnConnect.setBackgroundResource(R.drawable.btn_disconnect);
        } else {
            mBtnConnect.setText(getString(R.string.btn_connect));
            mBtnConnect.setEnabled(true);
            mBtnConnect.setBackgroundResource(R.drawable.btn_connect);
        }
        
        if (mStarting && mRunning) {
            mStarting = false;
        }
        
        if (mStopping && !mRunning) {
            mStopping = false;
        }
    }
    
    private void startVpn() {
        if (mProfile == null || mProfile.getServer() == null || mProfile.getServer().isEmpty()) {
            Toast.makeText(this, "Please configure server settings first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return;
        }
        
        mStarting = true;
        updateUI();
        
        Intent i = VpnService.prepare(this);
        
        if (i != null) {
            startActivityForResult(i, 0);
        } else {
            onActivityResult(0, Activity.RESULT_OK, null);
        }
    }
    
    private void stopVpn() {
        if (mBinder == null)
            return;
        
        mStopping = true;
        updateUI();
        
        try {
            mBinder.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        mBinder = null;
        unbindService(mConnection);
        checkState();
    }
}