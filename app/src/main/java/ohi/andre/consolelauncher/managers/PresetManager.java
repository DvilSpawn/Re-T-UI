package ohi.andre.consolelauncher.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ohi.andre.consolelauncher.managers.xml.AutoColorManager;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.classes.XMLPrefsSave;
import ohi.andre.consolelauncher.managers.xml.options.Suggestions;
import ohi.andre.consolelauncher.managers.xml.options.Theme;
import ohi.andre.consolelauncher.managers.xml.options.Ui;
import ohi.andre.consolelauncher.tuils.Tuils;

public final class PresetManager {

    private static final String PRESETS_FOLDER = "presets";

    private PresetManager() {}

    public static File getPresetsDir() {
        return new File(Tuils.getFolder(), PRESETS_FOLDER);
    }

    public static List<String> listPresets() {
        File[] files = getPresetsDir().listFiles(File::isDirectory);
        List<String> presets = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                presets.add(file.getName());
            }
        }
        Collections.sort(presets, String.CASE_INSENSITIVE_ORDER);
        return presets;
    }

    public static void save(String name) throws Exception {
        String cleanName = cleanName(name);
        File presetFolder = new File(getPresetsDir(), cleanName);
        if (!presetFolder.exists() && !presetFolder.mkdirs()) {
            throw new IllegalStateException("Unable to create preset folder");
        }

        boolean autoColor = XMLPrefsManager.getBoolean(Ui.auto_color_pick);
        writeXml(new File(presetFolder, XMLPrefsManager.XMLPrefsRoot.THEME.path),
                XMLPrefsManager.XMLPrefsRoot.THEME, Theme.values(), autoColor);
        writeXml(new File(presetFolder, XMLPrefsManager.XMLPrefsRoot.SUGGESTIONS.path),
                XMLPrefsManager.XMLPrefsRoot.SUGGESTIONS, Suggestions.values(), autoColor);

        apply(cleanName);
    }

    public static void apply(String name) throws Exception {
        String cleanName = cleanName(name);
        File presetFolder = new File(getPresetsDir(), cleanName);
        if (!presetFolder.isDirectory()) {
            throw new IllegalArgumentException("Preset not found");
        }

        File presetTheme = new File(presetFolder, XMLPrefsManager.XMLPrefsRoot.THEME.path);
        File presetSuggestions = new File(presetFolder, XMLPrefsManager.XMLPrefsRoot.SUGGESTIONS.path);
        if (!presetTheme.isFile() || !presetSuggestions.isFile()) {
            throw new IllegalArgumentException("Preset is incomplete");
        }

        File currentTheme = new File(Tuils.getFolder(), XMLPrefsManager.XMLPrefsRoot.THEME.path);
        File currentSuggestions = new File(Tuils.getFolder(), XMLPrefsManager.XMLPrefsRoot.SUGGESTIONS.path);
        Tuils.insertOld(currentTheme);
        Tuils.insertOld(currentSuggestions);
        Tuils.copy(presetTheme, currentTheme);
        Tuils.copy(presetSuggestions, currentSuggestions);
        XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.auto_color_pick, "false");
    }

    private static String cleanName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Preset name is required");
        }
        String cleanName = name.trim();
        if (cleanName.length() == 0 || cleanName.contains("/") || cleanName.contains("\\") || cleanName.contains("..")) {
            throw new IllegalArgumentException("Invalid preset name");
        }
        return cleanName;
    }

    private static void writeXml(File file, XMLPrefsManager.XMLPrefsRoot root, XMLPrefsSave[] values, boolean autoColor) throws Exception {
        StringBuilder xml = new StringBuilder(XMLPrefsManager.XML_DEFAULT);
        xml.append("<").append(root.name()).append(">\n");
        for (XMLPrefsSave value : values) {
            xml.append("\t<")
                    .append(value.label())
                    .append(" value=\"")
                    .append(resolveValue(value, autoColor))
                    .append("\" />\n");
        }
        xml.append("</").append(root.name()).append(">\n");

        FileOutputStream stream = new FileOutputStream(file, false);
        stream.write(xml.toString().getBytes());
        stream.flush();
        stream.close();
    }

    private static String resolveValue(XMLPrefsSave value, boolean autoColor) {
        if (autoColor) {
            int color = AutoColorManager.getAutoColor(value, Integer.MAX_VALUE);
            if (color != Integer.MAX_VALUE) {
                return String.format("#%08X", color);
            }
        }

        String current = XMLPrefsManager.get(value);
        if (current == null || current.length() == 0) {
            return value.defaultValue();
        }
        return current;
    }
}
