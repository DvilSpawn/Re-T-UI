package ohi.andre.consolelauncher.commands.tuixt;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.managers.xml.options.Ui;
import ohi.andre.consolelauncher.tuils.Tuils;

public class ThemerActivity extends AppCompatActivity {

    public static final String EXTRA_SECTION = "section";
    public static final String SECTION_HOME = "home";
    public static final String SECTION_THEME = "theme";
    public static final String SECTION_MUSIC = "music";

    private RecyclerView recyclerView;
    private String section;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        section = getIntent() != null ? getIntent().getStringExtra(EXTRA_SECTION) : null;
        if (section == null || section.length() == 0) {
            section = SECTION_HOME;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(16, 16, 16, 16);
        root.setFitsSystemWindows(true);

        TextView header = new TextView(this);
        header.setText("> " + getHeaderText(section));
        header.setTextColor(Color.GREEN);
        header.setTypeface(Typeface.MONOSPACE);
        header.setTextSize(18);
        root.addView(header);

        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#222222"));
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
        root.addView(divider);

        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String> items = getItemsForSection(section);

        recyclerView.setAdapter(new RecyclerView.Adapter<ViewHolder>() {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                TextView tv = new TextView(parent.getContext());
                tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv.setPadding(20, 30, 20, 30);
                tv.setTextColor(Color.WHITE);
                tv.setTypeface(Typeface.MONOSPACE);
                tv.setTextSize(16);
                return new ViewHolder(tv);
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                String fileName = items.get(position);
                ((TextView) holder.itemView).setText("- " + fileName);
                holder.itemView.setOnClickListener(v -> {
                    if (fileName.equals("Theme Settings")) {
                        openSection(SECTION_THEME);
                    } else if (fileName.equals("Music Settings")) {
                        openSection(SECTION_MUSIC);
                    } else if (fileName.equals("Open Wallpaper Picker")) {
                        launchWallpaperPicker();
                    } else if (fileName.equals("Open Live Wallpaper Picker")) {
                        launchLiveWallpaperPicker();
                    } else if (fileName.startsWith("Preferred Music App")) {
                        showPreferredMusicAppPicker();
                    } else if (fileName.equals("Fonts")) {
                        File tui = Tuils.getFolder();
                        File fontsDir = new File(tui, "fonts");
                        if (!fontsDir.exists()) {
                            fontsDir.mkdirs();
                        }

                        File[] fonts = fontsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".ttf") || name.toLowerCase().endsWith(".otf"));
                        
                        List<String> options = new ArrayList<>();
                        options.add("Default (System Font)");
                        if (fonts != null) {
                            for (File f : fonts) options.add(f.getName());
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(ThemerActivity.this);
                        builder.setTitle("Select a Font");
                        builder.setItems(options.toArray(new String[0]), (dialog, which) -> {
                            if (which == 0) {
                                applySystemFont();
                            } else {
                                applyFont(fonts[which - 1]);
                            }
                        });
                        builder.show();
                    } else if (fileName.equals("View Crash Log")) {
                        File crashFile = new File(Tuils.getFolder(), "crash.txt");
                        if (!crashFile.exists() || crashFile.length() == 0) {
                            Toast.makeText(ThemerActivity.this, "No crash log found.", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(ThemerActivity.this, TuixtActivity.class);
                            intent.putExtra(TuixtActivity.PATH, crashFile.getAbsolutePath());
                            startActivity(intent);
                        }
                    } else {
                        Intent intent = new Intent(ThemerActivity.this, TuixtActivity.class);
                        intent.putExtra(TuixtActivity.PATH, new File(Tuils.getFolder(), fileName).getAbsolutePath());
                        startActivityForResult(intent, LauncherActivity.TUIXT_REQUEST);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return items.size();
            }

            private void applySystemFont() {
                // Set system_font to true in ui.xml
                XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.system_font, "true");

                // Sweep any current font files in root to 'old'
                sweepCurrentFonts();

                finalizeFontChange("System font applied!");
            }

            private void applyFont(File source) {
                // Set system_font to false in ui.xml
                XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.system_font, "false");

                sweepCurrentFonts();

                // Copy selected font to root TUI folder
                File tuiFolder = Tuils.getFolder();
                File dest = new File(tuiFolder, source.getName());
                try {
                    Log.e("TUI-THEMER", "Copying font from " + source.getAbsolutePath() + " to " + dest.getAbsolutePath());
                    Tuils.copy(source, dest);
                    
                    if (dest.exists() && dest.length() > 0) {
                        Log.e("TUI-THEMER", "Copy successful! Size: " + dest.length());
                    } else {
                        Log.e("TUI-THEMER", "Copy failed or file is empty!");
                    }
                    
                    finalizeFontChange("Font applied!");
                } catch (Exception e) {
                    Toast.makeText(ThemerActivity.this, "Error applying font: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            private void sweepCurrentFonts() {
                File tuiFolder = Tuils.getFolder();
                File[] currentFiles = tuiFolder.listFiles();
                if (currentFiles != null) {
                    for (File f : currentFiles) {
                        String name = f.getName().toLowerCase();
                        if (name.endsWith(".ttf") || name.endsWith(".otf")) {
                            Tuils.insertOld(f);
                        }
                    }
                }
            }

            private void finalizeFontChange(String message) {
                // Clear font cache before reload
                Tuils.cancelFont();
                
                Toast.makeText(ThemerActivity.this, message + " Reloading...", Toast.LENGTH_SHORT).show();
                
                // Trigger reload with a slight delay for FS stability
                recyclerView.postDelayed(() -> {
                    if (LauncherActivity.instance != null) {
                        LauncherActivity.instance.reload();
                    }
                    finish();
                }, 500);
            }
        });

        root.addView(recyclerView);
        setContentView(root);
    }

    private String getHeaderText(String section) {
        if (SECTION_THEME.equals(section)) {
            return "Re:T-UI Theme Settings";
        } else if (SECTION_MUSIC.equals(section)) {
            return "Re:T-UI Music Settings";
        }
        return "Re:T-UI Settings Hub";
    }

    private List<String> getItemsForSection(String section) {
        if (SECTION_THEME.equals(section)) {
            return Arrays.asList(
                    "Open Wallpaper Picker",
                    "Open Live Wallpaper Picker",
                    "theme.xml",
                    "ui.xml",
                    "suggestions.xml",
                    "toolbar.xml",
                    "Fonts",
                    "View Crash Log"
            );
        } else if (SECTION_MUSIC.equals(section)) {
            return Arrays.asList("Preferred Music App: " + getPreferredMusicAppSummary());
        }

        return Arrays.asList(
                "Theme Settings",
                "Music Settings",
                "Open Wallpaper Picker",
                "Open Live Wallpaper Picker",
                "theme.xml",
                "ui.xml",
                "behavior.xml",
                "cmd.xml",
                "suggestions.xml",
                "toolbar.xml",
                "notifications.xml",
                "apps.xml",
                "rss.xml",
                "alias.txt",
                "ascii.txt",
                "Fonts",
                "View Crash Log"
        );
    }

    private void openSection(String targetSection) {
        Intent intent = new Intent(this, ThemerActivity.class);
        intent.putExtra(EXTRA_SECTION, targetSection);
        startActivity(intent);
    }

    private void launchWallpaperPicker() {
        try {
            startActivity(Intent.createChooser(new Intent(Intent.ACTION_SET_WALLPAPER), "Select wallpaper"));
        } catch (Exception e) {
            Toast.makeText(this, "Wallpaper picker is unavailable on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchLiveWallpaperPicker() {
        try {
            startActivity(new Intent(android.app.WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));
        } catch (Exception e) {
            Toast.makeText(this, "Live wallpaper picker is unavailable on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getPreferredMusicAppSummary() {
        String packageName = XMLPrefsManager.get(Behavior.preferred_music_app);
        if (packageName == null || packageName.length() == 0) {
            return "Auto detect";
        }

        PackageManager packageManager = getPackageManager();
        try {
            CharSequence label = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0));
            if (label != null && label.length() > 0) {
                return label + " (" + packageName + ")";
            }
        } catch (Exception ignored) {
        }

        return packageName;
    }

    private void showPreferredMusicAppPicker() {
        final List<AppChoice> choices = getLaunchableAppChoices();
        final List<String> labels = new ArrayList<>();
        labels.add("Auto detect");
        for (AppChoice choice : choices) {
            labels.add(choice.label + " (" + choice.packageName + ")");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Preferred Music App");
        builder.setItems(labels.toArray(new String[0]), (dialog, which) -> {
            if (which == 0) {
                XMLPrefsManager.XMLPrefsRoot.BEHAVIOR.write(Behavior.preferred_music_app, Tuils.EMPTYSTRING);
                Toast.makeText(this, "Preferred music app reset to automatic detection.", Toast.LENGTH_SHORT).show();
            } else {
                AppChoice choice = choices.get(which - 1);
                XMLPrefsManager.XMLPrefsRoot.BEHAVIOR.write(Behavior.preferred_music_app, choice.packageName);
                Toast.makeText(this, "Preferred music app set to " + choice.label + ".", Toast.LENGTH_SHORT).show();
            }
            recreate();
        });
        builder.show();
    }

    private List<AppChoice> getLaunchableAppChoices() {
        PackageManager packageManager = getPackageManager();
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolved = packageManager.queryIntentActivities(launcherIntent, 0);
        List<AppChoice> choices = new ArrayList<>();
        List<String> seenPackages = new ArrayList<>();

        for (ResolveInfo info : resolved) {
            if (info.activityInfo == null || info.activityInfo.packageName == null) {
                continue;
            }

            String packageName = info.activityInfo.packageName;
            if (seenPackages.contains(packageName)) {
                continue;
            }

            CharSequence loadedLabel = info.loadLabel(packageManager);
            String label = loadedLabel != null ? loadedLabel.toString() : packageName;
            choices.add(new AppChoice(label, packageName));
            seenPackages.add(packageName);
        }

        Collections.sort(choices, new Comparator<AppChoice>() {
            @Override
            public int compare(AppChoice left, AppChoice right) {
                return left.label.compareToIgnoreCase(right.label);
            }
        });

        return choices;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LauncherActivity.TUIXT_REQUEST && resultCode == TuixtActivity.SAVE_PRESSED) {
            if (LauncherActivity.instance != null) {
                LauncherActivity.instance.reload();
            }
            finish();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class AppChoice {
        final String label;
        final String packageName;

        AppChoice(String label, String packageName) {
            this.label = label;
            this.packageName = packageName;
        }
    }
}
