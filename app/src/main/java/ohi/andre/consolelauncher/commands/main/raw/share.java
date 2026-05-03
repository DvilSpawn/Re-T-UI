package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Intent;

import java.io.File;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.FileManager;
import ohi.andre.consolelauncher.managers.file.FileBackendManager;
import ohi.andre.consolelauncher.managers.termux.TermuxBridgeManager;
import ohi.andre.consolelauncher.tuils.Tuils;

public class share implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        String path = info.getString();
        if (FileBackendManager.activeBackend(info.context) == FileBackendManager.Active.TERMUX) {
            String resolved = resolvePath(info.currentDirectory, path);
            if (resolved == null) {
                return info.res.getString(helpRes());
            }
            TermuxBridgeManager.dispatchShell(info.context, "share " + resolved, tbridge.SHARE_FILE_SCRIPT, TermuxBridgeManager.TERMUX_HOME, resolved);
            return "Termux bridge sharing file: " + resolved;
        }

        File f = resolveNative(info.currentDirectory, path);
        if (f == null || !f.exists()) {
            return info.res.getString(R.string.output_filenotfound);
        }
        if (f.isDirectory())
            return info.res.getString(R.string.output_isdirectory);

        Intent sharingIntent = Tuils.shareFile(pack.context, f);
        info.context.startActivity(Intent.createChooser(sharingIntent, info.res.getString(R.string.share_label)));

        return Tuils.EMPTYSTRING;
    }

    @Override
    public int helpRes() {
        return R.string.help_share;
    }

    @Override
    public int[] argType() {
        return new int[]{CommandAbstraction.PLAIN_TEXT};
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        MainPack info = (MainPack) pack;
        return info.res.getString(helpRes());
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int index) {
        MainPack info = (MainPack) pack;
        return info.res.getString(R.string.output_filenotfound);
    }

    private String resolvePath(File currentDirectory, String path) {
        if (path == null || path.trim().length() == 0) {
            return null;
        }
        path = path.trim();
        File file = path.startsWith(File.separator) ? new File(path) : new File(currentDirectory, path);
        return file.getAbsolutePath();
    }

    private File resolveNative(File currentDirectory, String path) {
        FileManager.DirInfo dirInfo = FileManager.cd(currentDirectory, path);
        return dirInfo != null ? dirInfo.file : null;
    }

}
