package ohi.andre.consolelauncher.managers.settings;

import android.content.Context;
import android.graphics.Color;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ohi.andre.consolelauncher.managers.notifications.NotificationService;
import ohi.andre.consolelauncher.managers.xml.AutoColorManager;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.classes.XMLPrefsElement;
import ohi.andre.consolelauncher.managers.xml.classes.XMLPrefsSave;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.managers.xml.options.Notifications;
import ohi.andre.consolelauncher.managers.xml.options.Suggestions;
import ohi.andre.consolelauncher.managers.xml.options.Theme;
import ohi.andre.consolelauncher.managers.xml.options.Ui;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.UIUtils;

public final class LauncherSettings {

    private static final int NO_AUTO_COLOR = Integer.MAX_VALUE;
    private static final Object LOCK = new Object();
    private static final Map<String, String> snapshot = new HashMap<>();
    private static boolean snapshotLoaded = false;

    private LauncherSettings() {}

    public static String get(XMLPrefsSave value) {
        if (value == null) {
            return null;
        }

        synchronized (LOCK) {
            if (!snapshotLoaded) {
                refreshFromLoadedPrefsLocked();
            }

            String current = snapshot.get(key(value));
            if (current != null) {
                return current;
            }
        }

        return XMLPrefsManager.get(value);
    }

    public static boolean getBoolean(XMLPrefsSave value) {
        return Boolean.parseBoolean(getOrDefault(value));
    }

    public static int getInt(XMLPrefsSave value) {
        try {
            return Integer.parseInt(getOrDefault(value));
        } catch (Exception e) {
            return XMLPrefsManager.getInt(value);
        }
    }

    public static int getColor(XMLPrefsSave value) {
        int color;
        try {
            color = Color.parseColor(getOrDefault(value));
        } catch (Exception e) {
            color = XMLPrefsManager.getColor(value);
        }
        return AutoColorManager.getColor(value, color);
    }

    public static void refreshFromLoadedPrefs() {
        synchronized (LOCK) {
            refreshFromLoadedPrefsLocked();
        }
        AutoColorManager.invalidate();
    }

    public static void invalidate() {
        synchronized (LOCK) {
            snapshot.clear();
            snapshotLoaded = false;
        }
        AutoColorManager.invalidate();
    }

    public static String debugSummary() {
        synchronized (LOCK) {
            return "settings_snapshot_loaded: " + snapshotLoaded
                    + "\nsettings_snapshot_values: " + snapshot.size();
        }
    }

    public static void set(XMLPrefsSave value, String rawValue) {
        set(null, value, rawValue);
    }

    public static void set(Context context, XMLPrefsSave value, String rawValue) {
        if (value == null) {
            return;
        }

        XMLPrefsElement parent = value.parent();
        if (parent == null) {
            return;
        }

        parent.write(value, rawValue);
        synchronized (LOCK) {
            snapshot.put(key(value), rawValue);
            snapshotLoaded = true;
        }
        onSettingChanged(context, value);
    }

    public static void setTheme(Theme value, String rawValue) {
        set(value, rawValue);
    }

    public static void setSuggestion(Suggestions value, String rawValue) {
        set(value, rawValue);
    }

    public static void setUi(Ui value, String rawValue) {
        set(value, rawValue);
    }

    public static void setAutoColorPick(boolean enabled) {
        setUi(Ui.auto_color_pick, Boolean.toString(enabled));
    }

    public static String getEffective(XMLPrefsSave value) {
        if (getBoolean(Ui.auto_color_pick)) {
            int color = AutoColorManager.getAutoColor(value, NO_AUTO_COLOR);
            if (color != NO_AUTO_COLOR) {
                return String.format(Locale.US, "#%08X", color);
            }
        }

        String current = get(value);
        if (current == null || current.length() == 0) {
            return value.defaultValue();
        }
        return current;
    }

    private static String getOrDefault(XMLPrefsSave value) {
        String current = get(value);
        if (current == null || current.length() == 0) {
            return value != null ? value.defaultValue() : "";
        }
        return current;
    }

    private static void refreshFromLoadedPrefsLocked() {
        snapshot.clear();
        for (XMLPrefsManager.XMLPrefsRoot root : XMLPrefsManager.XMLPrefsRoot.values()) {
            for (XMLPrefsSave save : root.enums) {
                try {
                    String value = root.getValues().get(save).value;
                    if (value != null) {
                        snapshot.put(key(save), value);
                    }
                } catch (Exception ignored) {}
            }
        }
        snapshotLoaded = true;
    }

    private static String key(XMLPrefsSave value) {
        XMLPrefsElement parent = value.parent();
        String parentPath = parent != null ? parent.path() : "unknown";
        return parentPath + ":" + value.label();
    }

    private static void onSettingChanged(Context context, XMLPrefsSave value) {
        if (value == Ui.auto_color_pick) {
            AutoColorManager.invalidate();
        }

        if (value == Ui.system_font || value == Ui.font_file) {
            Tuils.cancelFont();
            UIUtils.cancelFont();
        }

        if (context == null) {
            return;
        }

        if (value instanceof Notifications || value == Behavior.preferred_music_app) {
            NotificationService.requestReload(context);
        }
    }
}
