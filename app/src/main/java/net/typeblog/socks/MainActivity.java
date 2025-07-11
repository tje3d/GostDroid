package net.typeblog.socks;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prepare the VPN connection (this will request permission if needed)
        Intent prepareIntent = VpnService.prepare(this);
        if (prepareIntent != null) {
            // Permission not yet granted, start the activity to request it
            startActivityForResult(prepareIntent, 0);
        }

        this.getFragmentManager().beginTransaction().replace(android.R.id.content, new ProfileFragment()).commit();
    }
}
