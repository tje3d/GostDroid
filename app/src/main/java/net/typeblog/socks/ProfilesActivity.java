package net.typeblog.socks;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import net.typeblog.socks.util.Profile;
import net.typeblog.socks.util.ProfileManager;

import java.util.Arrays;

public class ProfilesActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ProfileManager mManager;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);
        
        // Enable the Up button
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle("Profiles");
        }
        
        mManager = new ProfileManager(getApplicationContext());
        
        mListView = findViewById(R.id.profiles_list);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        
        findViewById(R.id.btn_add_profile).setOnClickListener(v -> addProfile());
        
        loadProfiles();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void loadProfiles() {
        String[] profiles = mManager.getProfiles();
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(profiles));
        mListView.setAdapter(mAdapter);
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String profileName = mAdapter.getItem(position);
        mManager.switchDefault(profileName);
        Toast.makeText(this, "Switched to profile: " + profileName, Toast.LENGTH_SHORT).show();
        finish();
    }
    
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        String profileName = mAdapter.getItem(position);
        showDeleteDialog(profileName);
        return true;
    }
    
    private void addProfile() {
        final EditText editText = new EditText(this);
        editText.setSingleLine(true);
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.prof_add)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    
                    if (!TextUtils.isEmpty(name)) {
                        Profile profile = mManager.addProfile(name);
                        
                        if (profile != null) {
                            loadProfiles();
                            return;
                        }
                    }
                    
                    Toast.makeText(this,
                            String.format(getString(R.string.err_add_prof), name),
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
    
    private void showDeleteDialog(String profileName) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.prof_del)
                .setMessage(String.format(getString(R.string.prof_del_confirm), profileName))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (!mManager.removeProfile(profileName)) {
                        Toast.makeText(this,
                                getString(R.string.err_del_prof, profileName),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        loadProfiles();
                        Toast.makeText(this, "Profile deleted: " + profileName, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}