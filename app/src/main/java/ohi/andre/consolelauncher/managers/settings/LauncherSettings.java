package ohi.andre.consolelauncher.managers.settings;

import android.content.Context;

import java.util.Locale;

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

public final class LauncherSettings {

    private static final int NO_AUTO_COLOR = Integer.MAX_VALUE;

    private LauncherSettings() {}

    public static String get(XMLPrefsSave value) {
        return XMLPrefsManager.get(value);
    }

    public static boolean getBoolean(Ui value) {
        return XMLPrefsManager.getBoolean(value);
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

    private static void onSettingChanged(Context context, XMLPrefsSave value) {
        if (value == Ui.auto_color_pick) {
            AutoColorManager.invalidate();
        }

        if (context == null) {
            return;
        }

        if (value instanceof Notifications || value == Behavior.preferred_music_app) {
            NotificationService.requestReload(context);
        }
    }
}
