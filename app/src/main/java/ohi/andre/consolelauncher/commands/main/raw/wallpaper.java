package ohi.andre.consolelauncher.commands.main.raw;

import android.app.WallpaperManager;
import android.content.Intent;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.main.specific.ParamCommand;
import ohi.andre.consolelauncher.managers.xml.AutoColorManager;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Ui;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.interfaces.Reloadable;

public class wallpaper extends ParamCommand {

    private enum Param implements ohi.andre.consolelauncher.commands.main.Param {
        static_wallpaper {
            @Override
            public String exec(ExecutePack pack) {
                return openStaticWallpaperPicker(pack);
            }

            @Override
            public String label() {
                return Tuils.MINUS + "static";
            }
        },
        live {
            @Override
            public String exec(ExecutePack pack) {
                try {
                    pack.context.startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));
                    return Tuils.EMPTYSTRING;
                } catch (Exception e) {
                    return pack.context.getString(R.string.output_error);
                }
            }
        },
        auto {
            @Override
            public String exec(ExecutePack pack) {
                AutoColorManager.invalidate();

                if (pack.context instanceof Reloadable) {
                    ((Reloadable) pack.context).addMessage("wallpaper", "Refreshed wallpaper-derived colors");
                    ((Reloadable) pack.context).reload();
                    return Tuils.EMPTYSTRING;
                }

                if (XMLPrefsManager.getBoolean(Ui.auto_color_pick)) {
                    return "Wallpaper-derived colors refreshed.";
                }
                return "Wallpaper palette cache cleared. Enable auto_color_pick to use wallpaper-derived colors.";
            }
        };

        @Override
        public int[] args() {
            return new int[0];
        }

        static Param get(String p) {
            p = p.toLowerCase();
            for (Param param : values()) {
                if (param.matches(p)) {
                    return param;
                }
            }
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

        boolean matches(String value) {
            if (value == null) {
                return false;
            }

            String label = label().toLowerCase();
            if (value.equals(label)) {
                return true;
            }

            if (label.startsWith(Tuils.MINUS)) {
                return value.equals(label.substring(1));
            }

            return false;
        }

        @Override
        public String onNotArgEnough(ExecutePack pack, int n) {
            return pack.context.getString(R.string.help_wallpaper);
        }

        @Override
        public String onArgNotFound(ExecutePack pack, int index) {
            return pack.context.getString(R.string.help_wallpaper);
        }
    }

    @Override
    protected ohi.andre.consolelauncher.commands.main.Param paramForString(MainPack pack, String param) {
        return Param.get(param);
    }

    @Override
    protected String doThings(ExecutePack pack) {
        if (pack.get(ohi.andre.consolelauncher.commands.main.Param.class, 0) != null) {
            return null;
        }
        return openStaticWallpaperPicker(pack);
    }

    private static String openStaticWallpaperPicker(ExecutePack pack) {
        try {
            pack.context.startActivity(Intent.createChooser(new Intent(Intent.ACTION_SET_WALLPAPER), pack.context.getString(R.string.app_name)));
            return Tuils.EMPTYSTRING;
        } catch (Exception e) {
            return pack.context.getString(R.string.output_error);
        }
    }

    @Override
    public String[] params() {
        return Param.labels();
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public int helpRes() {
        return R.string.help_wallpaper;
    }
}
