package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;

import ohi.andre.consolelauncher.MainManager;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.UIManager;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.file.FileBackendManager;
import ohi.andre.consolelauncher.managers.FileManager;
import ohi.andre.consolelauncher.managers.termux.TermuxBridgeManager;

public class cd implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        if (FileBackendManager.activeBackend(info.context) == FileBackendManager.Active.TERMUX) {
            String path = info.getString();
            if (path == null || path.trim().length() == 0) {
                return info.res.getString(helpRes());
            }
            String resolved = resolve(info.currentDirectory, path);
            TermuxBridgeManager.dispatchShell(info.context, "cd " + resolved, tbridge.CD_SCRIPT, TermuxBridgeManager.TERMUX_HOME, resolved);
            return "Termux bridge checking directory: " + resolved;
        }

        FileManager.DirInfo dirInfo = FileManager.cd(info.currentDirectory, info.getString());
        File folder = dirInfo != null ? dirInfo.file : null;
        if (folder == null || !folder.exists()) {
            return info.res.getString(R.string.output_filenotfound);
        }
        if (!folder.isDirectory()) {
            return "This is a file";
        }

        info.currentDirectory = folder;
        if (MainManager.interactive != null) {
            MainManager.interactive.addCommand("cd '" + folder.getAbsolutePath().replace("'", "'\\''") + "'");
        }
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
        return new int[]{CommandAbstraction.PLAIN_TEXT};
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

    private String resolve(File currentDirectory, String path) {
        path = path.trim();
        File file = path.startsWith(File.separator) ? new File(path) : new File(currentDirectory, path);
        return file.getAbsolutePath();
    }
}
