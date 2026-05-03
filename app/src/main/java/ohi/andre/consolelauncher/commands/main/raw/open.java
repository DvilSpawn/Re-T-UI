package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Intent;

import java.io.File;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.FileManager;
import ohi.andre.consolelauncher.managers.TerminalManager;
import ohi.andre.consolelauncher.managers.file.FileBackendManager;
import ohi.andre.consolelauncher.managers.termux.TermuxBridgeManager;
import ohi.andre.consolelauncher.tuils.Tuils;

public class open implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        String path = info.getString();
        if (FileBackendManager.activeBackend(info.context) == FileBackendManager.Active.TERMUX) {
            String resolved = resolvePath(info.currentDirectory, path);
            if (resolved == null) {
                return info.res.getString(helpRes());
            }
            TermuxBridgeManager.dispatchShell(info.context, "open " + resolved, tbridge.OPEN_FILE_SCRIPT, TermuxBridgeManager.TERMUX_HOME, "retui-open", resolved);
            return "Termux bridge opening file: " + resolved;
        }

        File file = resolve(info.currentDirectory, path);
        if (file == null || !file.exists()) {
            return info.res.getString(R.string.output_filenotfound);
        }
        if (file.isDirectory()) {
            return info.res.getString(R.string.output_isdirectory);
        }

        Intent view = Tuils.openFile(info.context, file);
        info.context.startActivity(Intent.createChooser(view, "Open with"));

        Tuils.sendOutput(info.context, "Opening: " + file.getName(), TerminalManager.CATEGORY_OUTPUT);
        return null;
    }

    private String resolvePath(File currentDirectory, String path) {
        if (path == null || path.trim().length() == 0) {
            return null;
        }
        path = path.trim();
        File file = path.startsWith(File.separator) ? new File(path) : new File(currentDirectory, path);
        return file.getAbsolutePath();
    }

    private File resolve(File currentDirectory, String path) {
        if (path == null || path.trim().length() == 0) {
            return null;
        }

        path = path.trim();
        File file = path.startsWith(File.separator) ? new File(path) : new File(currentDirectory, path);
        if (file.exists()) {
            return file;
        }

        FileManager.DirInfo dirInfo = FileManager.cd(currentDirectory, path);
        if (dirInfo != null && dirInfo.notFound == null) {
            return dirInfo.file;
        }
        return file;
    }

    @Override
    public int helpRes() {
        return R.string.help_open;
    }

    @Override
    public int[] argType() {
        return new int[]{CommandAbstraction.PLAIN_TEXT};
    }

    @Override
    public int priority() {
        return 4;
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

}
