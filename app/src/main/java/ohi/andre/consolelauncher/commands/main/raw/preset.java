package ohi.andre.consolelauncher.commands.main.raw;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.main.specific.ParamCommand;
import ohi.andre.consolelauncher.managers.xml.AutoColorManager;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Ui;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.interfaces.Reloadable;

public class preset extends ParamCommand {

    private enum Param implements ohi.andre.consolelauncher.commands.main.Param {
        save {
            @Override
            public String exec(ExecutePack pack) {
                String name = pack.getString();
                File presetsDir = new File(Tuils.getFolder(), "presets");
                if (!presetsDir.exists()) presetsDir.mkdirs();

                File presetFolder = new File(presetsDir, name);
                if (!presetFolder.exists()) presetFolder.mkdirs();

                File themeFile = new File(presetFolder, "theme.xml");
                File suggestionsFile = new File(presetFolder, "suggestions.xml");

                if (XMLPrefsManager.getBoolean(Ui.auto_color_pick)) {
                    // Generate from AutoColorManager
                    StringBuilder themeXml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<THEME>\n");
                    for (ohi.andre.consolelauncher.managers.xml.options.Theme theme : ohi.andre.consolelauncher.managers.xml.options.Theme.values()) {
                        int color = AutoColorManager.getAutoColor(theme, Integer.MAX_VALUE);
                        if (color != Integer.MAX_VALUE) {
                            themeXml.append("\t<").append(theme.label()).append(" value=\"").append(String.format("#%08X", color)).append("\" />\n");
                        } else {
                            themeXml.append("\t<").append(theme.label()).append(" value=\"").append(theme.defaultValue()).append("\" />\n");
                        }
                    }
                    themeXml.append("</THEME>");

                    StringBuilder suggXml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<SUGGESTIONS>\n");
                    for (ohi.andre.consolelauncher.managers.xml.options.Suggestions suggestion : ohi.andre.consolelauncher.managers.xml.options.Suggestions.values()) {
                        int color = AutoColorManager.getAutoColor(suggestion, Integer.MAX_VALUE);
                        if (color != Integer.MAX_VALUE) {
                            suggXml.append("\t<").append(suggestion.label()).append(" value=\"").append(String.format("#%08X", color)).append("\" />\n");
                        } else {
                            suggXml.append("\t<").append(suggestion.label()).append(" value=\"").append(suggestion.defaultValue()).append("\" />\n");
                        }
                    }
                    suggXml.append("</SUGGESTIONS>");

                    try {
                        Tuils.write(themeFile, themeXml.toString());
                        Tuils.write(suggestionsFile, suggXml.toString());
                    } catch (Exception e) {
                        return pack.context.getString(R.string.output_error);
                    }
                } else {
                    // Copy existing
                    File currentTheme = new File(Tuils.getFolder(), "theme.xml");
                    File currentSuggestions = new File(Tuils.getFolder(), "suggestions.xml");
                    try {
                        if (currentTheme.exists()) Tuils.copy(currentTheme, themeFile);
                        if (currentSuggestions.exists()) Tuils.copy(currentSuggestions, suggestionsFile);
                    } catch (Exception e) {
                        return pack.context.getString(R.string.output_error);
                    }
                }

                return "Preset '" + name + "' saved successfully.";
            }

            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.PLAIN_TEXT};
            }
        },
        apply {
            @Override
            public String exec(ExecutePack pack) {
                String name = pack.getString();
                File presetFolder = new File(Tuils.getFolder(), "presets/" + name);
                if (!presetFolder.exists() || !presetFolder.isDirectory()) {
                    return "Preset not found.";
                }

                File themeFile = new File(presetFolder, "theme.xml");
                File suggestionsFile = new File(presetFolder, "suggestions.xml");

                File currentTheme = new File(Tuils.getFolder(), "theme.xml");
                File currentSuggestions = new File(Tuils.getFolder(), "suggestions.xml");

                try {
                    if (themeFile.exists()) Tuils.copy(themeFile, currentTheme);
                    if (suggestionsFile.exists()) Tuils.copy(suggestionsFile, currentSuggestions);
                    
                    // Disable auto_color_pick
                    XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.auto_color_pick, "false");

                    if (pack.context instanceof Reloadable) {
                        ((Reloadable) pack.context).addMessage("preset", "Applied preset: " + name);
                        ((Reloadable) pack.context).reload();
                    }

                    return "Preset applied!";
                } catch (Exception e) {
                    return pack.context.getString(R.string.output_error);
                }
            }

            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.PLAIN_TEXT};
            }
        },
        ls {
            @Override
            public String exec(ExecutePack pack) {
                File presetsDir = new File(Tuils.getFolder(), "presets");
                if (!presetsDir.exists() || presetsDir.listFiles() == null) {
                    return "No presets found.";
                }
                List<String> list = new ArrayList<>();
                for (File f : presetsDir.listFiles()) {
                    if (f.isDirectory()) list.add(f.getName());
                }
                if (list.isEmpty()) return "No presets found.";
                return Tuils.toPlanString(list, "\n");
            }

            @Override
            public int[] args() {
                return new int[0];
            }
        };

        static Param get(String p) {
            p = p.toLowerCase();
            Param[] ps = values();
            for (Param p1 : ps)
                if (p.endsWith(p1.label()))
                    return p1;
            return null;
        }

        static String[] labels() {
            Param[] ps = values();
            String[] ss = new String[ps.length];

            for (int count = 0; count < ps.length; count++) {
                ss[count] = ps[count].label();
            }

            return ss;
        }

        @Override
        public String label() {
            return Tuils.MINUS + name();
        }

        @Override
        public String onNotArgEnough(ExecutePack pack, int n) {
            return pack.context.getString(R.string.help_preset);
        }

        @Override
        public String onArgNotFound(ExecutePack pack, int index) {
            return pack.context.getString(R.string.help_preset);
        }
    }

    @Override
    public String[] params() {
        return Param.labels();
    }

    @Override
    protected ohi.andre.consolelauncher.commands.main.Param paramForString(MainPack pack, String param) {
        return Param.get(param);
    }

    @Override
    public int priority() {
        return 4;
    }

    @Override
    public int helpRes() {
        return R.string.help_preset;
    }

    @Override
    protected String doThings(ExecutePack pack) {
        return null;
    }
}
