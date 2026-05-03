package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Intent;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.tuixt.ThemerActivity;
import ohi.andre.consolelauncher.tuils.Tuils;

public class settings implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
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
    public int[] argType() {
        return new int[0];
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public int helpRes() {
        return R.string.help_settings;
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int index) {
        return pack.context.getString(R.string.help_settings);
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return exec(pack);
    }
}
