package net.typeblog.socks;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import net.typeblog.socks.util.Routes;
import net.typeblog.socks.util.Utility;

import java.util.Locale;
import java.util.Objects;

import static net.typeblog.socks.util.Constants.*;
import static net.typeblog.socks.BuildConfig.DEBUG;

public class SocksVpnService extends VpnService {
    class VpnBinder extends IVpnService.Stub {
        @Override
        public boolean isRunning() {
            return mRunning;
        }

        @Override
        public void stop() {
            stopMe();
        }
    }

    private static final String TAG = SocksVpnService.class.getSimpleName();

    private ParcelFileDescriptor mInterface;
    private boolean mRunning = false;
    private final IBinder mBinder = new VpnBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (DEBUG) {
            Log.d(TAG, "starting");
        }

        if (intent == null) {
            return START_STICKY;
        }

        if (mRunning) {
            return START_STICKY;
        }

        final String name = intent.getStringExtra(INTENT_NAME);
        final String server = intent.getStringExtra(INTENT_SERVER);
        final int port = intent.getIntExtra(INTENT_PORT, 1080);
        final String username = intent.getStringExtra(INTENT_USERNAME);
        final String passwd = intent.getStringExtra(INTENT_PASSWORD);
        final String route = intent.getStringExtra(INTENT_ROUTE);
        final String dns = intent.getStringExtra(INTENT_DNS);
        final int dnsPort = intent.getIntExtra(INTENT_DNS_PORT, 53);
        final boolean perApp = intent.getBooleanExtra(INTENT_PER_APP, false);
        final boolean appBypass = intent.getBooleanExtra(INTENT_APP_BYPASS, false);
        final String[] appList = intent.getStringArrayExtra(INTENT_APP_LIST);
        final boolean ipv6 = intent.getBooleanExtra(INTENT_IPV6_PROXY, false);
        final String udpgw = intent.getStringExtra(INTENT_UDP_GW);
        final boolean useGost = intent.getBooleanExtra(INTENT_USE_GOST, false);
        final String gostTransport = intent.getStringExtra(INTENT_GOST_TRANSPORT);
        final String gostServer = intent.getStringExtra(INTENT_GOST_SERVER);
        final String gostUsername = intent.getStringExtra(INTENT_GOST_USERNAME);
        final String gostPassword = intent.getStringExtra(INTENT_GOST_PASSWORD);

        // Notifications on Oreo and above need a channel
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            String NOTIFICATION_CHANNEL_ID = "net.typeblog.socks";
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    getString(R.string.channel_name), NotificationManager.IMPORTANCE_NONE);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
            builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        // Create the notification
        int NOTIFICATION_ID = 1;
        int intentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            intentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent contentIntent;
        contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), intentFlags);
        startForeground(NOTIFICATION_ID, builder
                .setContentTitle(getString(R.string.notify_title))
                .setContentText(String.format(  getString(R.string.notify_msg), name))
                .setPriority(Notification.PRIORITY_MIN)
                .setSmallIcon(R.drawable.ic_vpn)
                .setContentIntent(contentIntent)
                .build());

        // Create an fd.
        configure(name, route, perApp, appBypass, appList, ipv6);

        if (DEBUG)
            Log.d(TAG, "fd: " + mInterface.getFd());

        if (mInterface != null)
            start(mInterface.getFd(), server, port, username, passwd, dns, dnsPort, ipv6, udpgw, useGost, gostTransport, gostServer, gostUsername, gostPassword);

        return START_STICKY;
    }

    @Override
    public void onRevoke() {
        super.onRevoke();
        stopMe();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopMe();
    }

    private void stopMe() {
        stopForeground(true);

        Utility.killPidFile(getFilesDir() + "/tun2socks.pid");
        Utility.killPidFile(getFilesDir() + "/pdnsd.pid");

        // Stop Gost tunnel if it's running
        GostTunnel.stopTunnel();

        try {
            System.jniclose(mInterface.getFd());
            mInterface.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        stopSelf();
    }

    private void configure(String name, String route, boolean perApp, boolean bypass, String[] apps, boolean ipv6) {
        Builder b = new Builder();
        b.setMtu(1500)
                .setSession(name)
                .addAddress("26.26.26.1", 24)
                .addDnsServer("8.8.8.8");

        if (ipv6) {
            // Route all IPv6 traffic
            b.addAddress("fdfe:dcba:9876::1", 126)
                    .addRoute("::", 0);
        }

        Routes.addRoutes(this, b, route);

        // Add the default DNS
        // Note that this DNS is just a stub.
        // Actual DNS requests will be redirected through pdnsd.
        b.addRoute("8.8.8.8", 32);

        // Do app routing
        if (!perApp) {
            // Just bypass myself
            try {
                b.addDisallowedApplication("net.typeblog.socks");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (bypass) {
                // First, bypass myself
                try {
                    b.addDisallowedApplication("net.typeblog.socks");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for (String p : apps) {
                    if (TextUtils.isEmpty(p))
                        continue;

                    try {
                        b.addDisallowedApplication(p.trim());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                for (String p : apps) {
                    if (TextUtils.isEmpty(p) || p.trim().equals("net.typeblog.socks")) {
                        continue;
                    }

                    try {
                        b.addAllowedApplication(p.trim());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        mInterface = b.establish();
    }

    private void start(int fd, String server, int port, String user, String passwd, String dns, int dnsPort, boolean ipv6, String udpgw, boolean useGost, String gostTransport, String gostServer, String gostUsername, String gostPassword) {
        // Start DNS daemon first
        Utility.makePdnsdConf(this, dns, dnsPort);

        Utility.exec(String.format(Locale.US, "%s/libpdnsd.so -c %s/pdnsd.conf",
                getApplicationInfo().nativeLibraryDir, getFilesDir()));

        if (useGost) {
            // Use Gost tunnel instead of traditional tun2socks
            // Use the dedicated GOST server address if provided, otherwise fall back to server:port
            String serverAddr = (gostServer != null && !gostServer.isEmpty()) ? gostServer : server + ":" + port;
            String transport = gostTransport != null ? gostTransport : "ws";
            
            if (DEBUG) {
                Log.d(TAG, "Starting Gost tunnel with transport: " + transport + ", server: " + serverAddr + 
                      (gostUsername != null && !gostUsername.isEmpty() ? ", username: " + gostUsername : ""));
            }
            
            // Use authentication if provided, otherwise use empty credentials
            String username = (gostUsername != null) ? gostUsername : "";
            String password = (gostPassword != null) ? gostPassword : "";
            
            if (GostTunnel.startTunnel(transport, serverAddr, username, password)) {
                // Gost tunnel started successfully, now connect tun2socks to local Gost SOCKS5 proxy
                String command = String.format(Locale.US,
                        "%s/libtun2socks.so --netif-ipaddr 26.26.26.2"
                                + " --netif-netmask 255.255.255.0"
                                + " --socks-server-addr 127.0.0.1:1080"  // Connect to local Gost proxy
                                + " --tunfd %d"
                                + " --tunmtu 1500"
                                + " --loglevel 3"
                                + " --pid %s/tun2socks.pid"
                                + " --sock %s/sock_path"
                        , getApplicationInfo().nativeLibraryDir, fd, getFilesDir(), getApplicationInfo().dataDir);

                if (ipv6) {
                    command += " --netif-ip6addr fdfe:dcba:9876::2";
                }

                command += " --dnsgw 26.26.26.1:8091";

                if (DEBUG) {
                    Log.d(TAG, command);
                }

                if (Utility.exec(command) != 0) {
                    GostTunnel.stopTunnel();
                    stopMe();
                    return;
                }
            } else {
                Log.e(TAG, "Failed to start Gost tunnel");
                stopMe();
                return;
            }
        } else {
            // Use traditional tun2socks approach
            String command = String.format(Locale.US,
                    "%s/libtun2socks.so --netif-ipaddr 26.26.26.2"
                            + " --netif-netmask 255.255.255.0"
                            + " --socks-server-addr %s:%d"
                            + " --tunfd %d"
                            + " --tunmtu 1500"
                            + " --loglevel 3"
                            + " --pid %s/tun2socks.pid"
                            + " --sock %s/sock_path"
                    , getApplicationInfo().nativeLibraryDir, server, port, fd, getFilesDir(), getApplicationInfo().dataDir);

            if (user != null) {
                command += " --username " + user;
                command += " --password " + passwd;
            }

            if (ipv6) {
                command += " --netif-ip6addr fdfe:dcba:9876::2";
            }

            command += " --dnsgw 26.26.26.1:8091";

            if (udpgw != null) {
                command += " --udpgw-remote-server-addr " + udpgw;
            }

            if (DEBUG) {
                Log.d(TAG, command);
            }

            if (Utility.exec(command) != 0) {
                stopMe();
                return;
            }
        }

        // Try to send the Fd through socket.
        int i = 0;
        while (i < 5) {
            if (System.sendfd(fd, getApplicationInfo().dataDir + "/sock_path") != -1) {
                mRunning = true;
                return;
            }

            i++;

            try {
                Thread.sleep(1000L * i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Should not get here. Must be a failure.
        stopMe();
    }
}
