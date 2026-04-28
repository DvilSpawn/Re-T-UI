package ohi.andre.consolelauncher.managers.settings;

import ohi.andre.consolelauncher.managers.xml.options.Theme;
import ohi.andre.consolelauncher.managers.xml.options.Ui;

public final class AppearanceSettings {

    private AppearanceSettings() {}

    public static boolean autoColorPick() {
        return LauncherSettings.getBoolean(Ui.auto_color_pick);
    }

    public static boolean useSystemFont() {
        return LauncherSettings.getBoolean(Ui.system_font);
    }

    public static String fontFile() {
        return LauncherSettings.get(Ui.font_file);
    }

    public static int musicWidgetColor() {
        return LauncherSettings.getColor(Theme.music_widget_color);
    }

    public static int musicWidgetBorderColor() {
        return LauncherSettings.getColor(Theme.music_widget_border_color);
    }

    public static int musicWidgetTextColor() {
        return LauncherSettings.getColor(Theme.music_widget_text_color);
    }

    public static int notificationWidgetBorderColor() {
        return LauncherSettings.getColor(Theme.notification_widget_border_color);
    }

    public static int notificationWidgetTextColor() {
        return LauncherSettings.getColor(Theme.notification_widget_text_color);
    }

    public static int terminalWindowBackground() {
        return LauncherSettings.getColor(Theme.window_terminal_bg);
    }

    public static boolean dashedBorders() {
        return LauncherSettings.getBoolean(Ui.enable_dashed_border);
    }

    public static int dashLength() {
        return LauncherSettings.getInt(Ui.dashed_border_dash_length);
    }

    public static int dashGap() {
        return LauncherSettings.getInt(Ui.dashed_border_gap_length);
    }
}
