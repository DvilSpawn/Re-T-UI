package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.UIManager;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;

public class hack implements CommandAbstraction {

    @Override
    public String exec(ExecutePack info) {
        LocalBroadcastManager.getInstance(info.context.getApplicationContext())
                .sendBroadcast(new Intent(UIManager.ACTION_HACK));
        return "Injecting cinematic nonsense...";
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
        return R.string.help_hack;
    }

    @Override
    public String onArgNotFound(ExecutePack info, int index) {
        return null;
    }

    @Override
    public String onNotArgEnough(ExecutePack info, int nArgs) {
        return null;
    }
}
