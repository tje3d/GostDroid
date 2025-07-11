package net.typeblog.socks.util;

import android.content.SharedPreferences;

import static net.typeblog.socks.util.Constants.*;

public class Profile {
    private final SharedPreferences mPref;
    private final String mName;
    private final String mPrefix;

    Profile(SharedPreferences pref, String name) {
        mPref = pref;
        mName = name;
        mPrefix = prefPrefix(name);
    }

    public String getName() {
        return mName;
    }

    public String getServer() {
        return mPref.getString(key("server"), "127.0.0.1");
    }

    public void setServer(String server) {
        mPref.edit().putString(key("server"), server).apply();
    }

    public int getPort() {
        return mPref.getInt(key("port"), 1080);
    }

    public void setPort(int port) {
        mPref.edit().putInt(key("port"), port).apply();
    }

    public boolean isUserPw() {
        return mPref.getBoolean(key("userpw"), false);
    }

    public void setIsUserpw(boolean is) {
        mPref.edit().putBoolean(key("userpw"), is).apply();
    }

    public String getUsername() {
        return mPref.getString(key("username"), "");
    }

    public void setUsername(String username) {
        mPref.edit().putString(key("username"), username).apply();
    }

    public String getPassword() {
        return mPref.getString(key("password"), "");
    }

    public void setPassword(String password) {
        mPref.edit().putString(key("password"), password).apply();
    }

    public String getRoute() {
        return mPref.getString(key("route"), ROUTE_ALL);
    }

    public void setRoute(String route) {
        mPref.edit().putString(key("route"), route).apply();
    }

    public String getDns() {
        return mPref.getString(key("dns"), "8.8.8.8");
    }

    public void setDns(String dns) {
        mPref.edit().putString(key("dns"), dns).apply();
    }

    public int getDnsPort() {
        return mPref.getInt(key("dns_port"), 53);
    }

    public void setDnsPort(int port) {
        mPref.edit().putInt(key("dns_port"), port).apply();
    }

    public boolean isPerApp() {
        return mPref.getBoolean(key("perapp"), true);
    }

    public void setIsPerApp(boolean is) {
        mPref.edit().putBoolean(key("perapp"), is).apply();
    }

    public boolean isBypassApp() {
        return mPref.getBoolean(key("appbypass"), true);
    }

    public void setIsBypassApp(boolean is) {
        mPref.edit().putBoolean(key("appbypass"), is).apply();
    }

    public String getAppList() {
        return mPref.getString(key("applist"), "com.termux");
    }

    public void setAppList(String list) {
        mPref.edit().putString(key("applist"), list).apply();
    }

    public boolean hasIPv6() {
        return mPref.getBoolean(key("ipv6"), false);
    }

    public void setHasIPv6(boolean has) {
        mPref.edit().putBoolean(key("ipv6"), has).apply();
    }

    public boolean hasUDP() {
        return mPref.getBoolean(key("udp"), true);
    }

    public void setHasUDP(boolean has) {
        mPref.edit().putBoolean(key("udp"), has).apply();
    }

    public String getUDPGW() {
        return mPref.getString(key("udpgw"), "127.0.0.1:7300");
    }

    public void setUDPGW(String gw) {
        mPref.edit().putString(key("udpgw"), gw).apply();
    }

    public boolean autoConnect() {
        return mPref.getBoolean(key("auto"), false);
    }

    public void setAutoConnect(boolean auto) {
        mPref.edit().putBoolean(key("auto"), auto).apply();
    }

    public boolean useGost() {
        return mPref.getBoolean(key("use_gost"), false);
    }

    public void setUseGost(boolean use) {
        mPref.edit().putBoolean(key("use_gost"), use).apply();
    }

    public String getGostTransport() {
        return mPref.getString(key("gost_transport"), "ws");
    }

    public void setGostTransport(String transport) {
        mPref.edit().putString(key("gost_transport"), transport).apply();
    }

    public String getGostServer() {
        return mPref.getString(key("gost_server"), "");
    }

    public void setGostServer(String server) {
        mPref.edit().putString(key("gost_server"), server).apply();
    }

    public String getGostUsername() {
        return mPref.getString(key("gost_username"), "");
    }

    public void setGostUsername(String username) {
        mPref.edit().putString(key("gost_username"), username).apply();
    }

    public String getGostPassword() {
        return mPref.getString(key("gost_password"), "");
    }

    public void setGostPassword(String password) {
        mPref.edit().putString(key("gost_password"), password).apply();
    }

    void delete() {
        mPref.edit()
                .remove(key("server"))
                .remove(key("port"))
                .remove(key("userpw"))
                .remove(key("username"))
                .remove(key("password"))
                .remove(key("route"))
                .remove(key("dns"))
                .remove(key("dns_port"))
                .remove(key("perapp"))
                .remove(key("appbypass"))
                .remove(key("applist"))
                .remove(key("ipv6"))
                .remove(key("udp"))
                .remove(key("udpgw"))
                .remove(key("auto"))
                .remove(key("use_gost"))
                .remove(key("gost_transport"))
                .remove(key("gost_server"))
                .remove(key("gost_username"))
                .remove(key("gost_password"))
                .apply();
    }

    private String key(String k) {
        return mPrefix + k;
    }

    private static String prefPrefix(String name) {
        return name.replace("_", "__").replace(" ", "_");
    }
}
