package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.UIManager;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;

public class cd implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        File folder = info.get(File.class);
        if (folder == null || !folder.exists()) {
            return info.res.getString(R.string.output_filenotfound);
        }
        if (!folder.isDirectory()) {
            return "This is a file";
        }

        info.currentDirectory = folder;
        LocalBroadcastManager.getInstance(info.context.getApplicationContext())
                .sendBroadcast(new Intent(UIManager.ACTION_UPDATE_HINT));
        return folder.getAbsolutePath();
    }

    @Override
    public int helpRes() {
        return R.string.help_cd;
    }

    @Override
    public int[] argType() {
        return new int[]{CommandAbstraction.FILE};
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return pack.context.getString(helpRes());
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int index) {
        return ((MainPack) pack).res.getString(R.string.output_filenotfound);
    }
}
