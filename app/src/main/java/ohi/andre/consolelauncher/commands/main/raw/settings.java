package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Intent;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.main.specific.ParamCommand;
import ohi.andre.consolelauncher.commands.tuixt.ThemerActivity;
import ohi.andre.consolelauncher.tuils.Tuils;

public class settings extends ParamCommand {

    private enum Param implements ohi.andre.consolelauncher.commands.main.Param {
        theme {
            @Override
            public String exec(ExecutePack pack) {
                return openSettings(pack, ThemerActivity.SECTION_THEME);
            }
        },
        music {
            @Override
            public String exec(ExecutePack pack) {
                return openSettings(pack, ThemerActivity.SECTION_MUSIC);
            }
        };

        @Override
        public int[] args() {
            return new int[0];
        }

        static Param get(String p) {
            p = p.toLowerCase();
            for (Param param : values()) {
                if (matches(p, param.label())) {
                    return param;
                }
            }
            return null;
        }

        static boolean matches(String value, String label) {
            if (value == null || label == null) {
                return false;
            }

            if (value.endsWith(label)) {
                return true;
            }

            if (label.startsWith(Tuils.MINUS)) {
                return value.equals(label.substring(1));
            }

            return false;
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
            return pack.context.getString(R.string.help_settings);
        }

        @Override
        public String onArgNotFound(ExecutePack pack, int index) {
            return pack.context.getString(R.string.help_settings);
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
        return openSettings(pack, ThemerActivity.SECTION_HOME);
    }

    private static String openSettings(ExecutePack pack, String section) {
        MainPack info = (MainPack) pack;
        Intent intent = new Intent(info.context, ThemerActivity.class);
        intent.putExtra(ThemerActivity.EXTRA_SECTION, section);
        info.context.startActivity(intent);
        return Tuils.EMPTYSTRING;
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
        return R.string.help_settings;
    }
}
