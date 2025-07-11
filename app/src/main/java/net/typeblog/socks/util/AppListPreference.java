package net.typeblog.socks.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.typeblog.socks.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppListPreference extends Preference {
    private List<String> mPackageNames;
    private String mValue = "";

    public AppListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPackageNames = new ArrayList<>();
        setLayoutResource(R.layout.preference_app_list);
    }

    public AppListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPackageNames = new ArrayList<>();
        setLayoutResource(R.layout.preference_app_list);
    }

    @Override
    protected void onClick() {
        showAppListDialog();
    }

    public void setValue(String value) {
        mValue = value;
        mPackageNames.clear();
        if (value != null && !value.trim().isEmpty()) {
            String[] packages = value.split("\n");
            for (String pkg : packages) {
                String trimmed = pkg.trim();
                if (!trimmed.isEmpty()) {
                    mPackageNames.add(trimmed);
                }
            }
        }
        updateSummary();
        persistString(value);
    }

    public String getValue() {
        return mValue;
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            setValue(getPersistedString("com.termux"));
        } else {
            setValue((String) defaultValue);
        }
    }

    private void updateSummary() {
        if (mPackageNames.isEmpty()) {
            setSummary("No apps selected");
        } else if (mPackageNames.size() == 1) {
            setSummary("1 app: " + mPackageNames.get(0));
        } else {
            setSummary(mPackageNames.size() + " apps selected");
        }
    }

    private void showAppListDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_app_list, null);

        ListView listView = dialogView.findViewById(R.id.app_list);
        Button addButton = dialogView.findViewById(R.id.btn_add_app);
        Button removeButton = dialogView.findViewById(R.id.btn_remove_app);

        // Create adapter for the list
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), 
            R.layout.list_item_app_package, mPackageNames);
        
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Add button functionality
        addButton.setOnClickListener(v -> showAddPackageDialog(adapter));

        // Remove button functionality
        removeButton.setOnClickListener(v -> {
            int selectedPosition = listView.getCheckedItemPosition();
            if (selectedPosition != ListView.INVALID_POSITION) {
                mPackageNames.remove(selectedPosition);
                adapter.notifyDataSetChanged();
                listView.clearChoices();
            } else {
                Toast.makeText(getContext(), "Please select an app to remove", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Manage App List")
                .setView(dialogView)
                .setPositiveButton("Save", (d, which) -> {
                    // Update the value
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mPackageNames.size(); i++) {
                        if (i > 0) sb.append("\n");
                        sb.append(mPackageNames.get(i));
                    }
                    String newValue = sb.toString();
                    if (!newValue.equals(mValue)) {
                        setValue(newValue);
                        callChangeListener(newValue);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }

    private void showAddPackageDialog(ArrayAdapter<String> adapter) {
        EditText editText = new EditText(getContext());
        editText.setHint("com.example.app");
        editText.setSingleLine(true);

        AlertDialog addDialog = new AlertDialog.Builder(getContext())
                .setTitle("Add Package Name")
                .setMessage("Enter the package name of the app:")
                .setView(editText)
                .setPositiveButton("Add", (d, which) -> {
                    String packageName = editText.getText().toString().trim();
                    if (!packageName.isEmpty()) {
                        if (!mPackageNames.contains(packageName)) {
                            mPackageNames.add(packageName);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "Package already exists", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Please enter a valid package name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Common Apps", (d, which) -> {
                    showCommonAppsDialog(adapter);
                })
                .setNegativeButton("Cancel", null)
                .create();

        addDialog.show();
    }

    private void showCommonAppsDialog(ArrayAdapter<String> adapter) {
        String[] commonApps = {
            "com.termux",
            "com.android.chrome",
            "com.google.android.apps.messaging",
            "com.whatsapp",
            "com.telegram.messenger",
            "com.discord",
            "com.spotify.music",
            "com.netflix.mediaclient",
            "com.youtube.android",
            "com.instagram.android",
            "com.twitter.android",
            "com.facebook.katana",
            "com.snapchat.android",
            "com.tiktok.android",
            "com.reddit.frontpage",
            "com.skype.raider",
            "com.viber.voip",
            "com.microsoft.teams",
            "com.zoom.us",
            "com.google.android.gm",
            "com.microsoft.office.outlook"
        };

        String[] appNames = {
            "Termux",
            "Chrome",
            "Messages",
            "WhatsApp",
            "Telegram",
            "Discord",
            "Spotify",
            "Netflix",
            "YouTube",
            "Instagram",
            "Twitter",
            "Facebook",
            "Snapchat",
            "TikTok",
            "Reddit",
            "Skype",
            "Viber",
            "Microsoft Teams",
            "Zoom",
            "Gmail",
            "Outlook"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Common Apps");
        
        boolean[] checkedItems = new boolean[commonApps.length];
        
        builder.setMultiChoiceItems(appNames, checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
        });
        
        builder.setPositiveButton("Add Selected", (dialog, which) -> {
            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i] && !mPackageNames.contains(commonApps[i])) {
                    mPackageNames.add(commonApps[i]);
                }
            }
            adapter.notifyDataSetChanged();
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }
}