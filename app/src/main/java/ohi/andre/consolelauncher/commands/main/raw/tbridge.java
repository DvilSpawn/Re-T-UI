package ohi.andre.consolelauncher.commands.main.raw;

import java.io.File;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.termux.TermuxBridgeManager;

public class tbridge implements CommandAbstraction {

    private static final String LIST_DIRS_SCRIPT =
            "dir=\"$1\"; [ -d \"$dir\" ] || { echo \"not a directory: $dir\" >&2; exit 2; }; "
                    + "find \"$dir\" -mindepth 1 -maxdepth 1 -type d -printf '%f/\\n' 2>/dev/null | sort";
    private static final String LIST_FILES_SCRIPT =
            "dir=\"$1\"; [ -d \"$dir\" ] || { echo \"not a directory: $dir\" >&2; exit 2; }; "
                    + "find \"$dir\" -mindepth 1 -maxdepth 1 -type f -printf '%f\\n' 2>/dev/null | sort";
    private static final String LIST_ALL_SCRIPT =
            "dir=\"$1\"; [ -d \"$dir\" ] || { echo \"not a directory: $dir\" >&2; exit 2; }; "
                    + "{ find \"$dir\" -mindepth 1 -maxdepth 1 -type d -printf '%f/\\n' 2>/dev/null; "
                    + "find \"$dir\" -mindepth 1 -maxdepth 1 -type f -printf '%f\\n' 2>/dev/null; } | sort";
    private static final String STATUS_SCRIPT =
            "printf 'termux_home=%s\\n' \"$HOME\"; "
                    + "printf 'pwd=%s\\n' \"$PWD\"; "
                    + "command -v find >/dev/null && echo 'find=available' || echo 'find=missing'; "
                    + "[ -d /storage/emulated/0 ] && echo 'shared_storage=visible' || echo 'shared_storage=not_visible'";

    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        String input = info.getString();
        if (input == null || input.trim().length() == 0) {
            return info.res.getString(helpRes());
        }

        String[] parts = input.trim().split("\\s+", 2);
        String option = parts[0].toLowerCase();
        String path = parts.length > 1 ? parts[1].trim() : null;

        if ("-status".equals(option)) {
            return localStatus(info);
        }

        if ("-setup".equals(option)) {
            return setupText();
        }

        if (!ensureReady(info)) {
            return null;
        }

        if ("-probe".equals(option)) {
            TermuxBridgeManager.dispatchShell(info.context, "probe", STATUS_SCRIPT, TermuxBridgeManager.TERMUX_HOME);
            return "Termux bridge probe dispatched.";
        }

        String resolved = resolvePath(info, path);
        if ("-dirs".equals(option)) {
            TermuxBridgeManager.dispatchShell(info.context, "dirs " + resolved, LIST_DIRS_SCRIPT, TermuxBridgeManager.TERMUX_HOME, "retui-dirs", resolved);
            return "Termux bridge listing directories: " + resolved;
        }

        if ("-files".equals(option)) {
            TermuxBridgeManager.dispatchShell(info.context, "files " + resolved, LIST_FILES_SCRIPT, TermuxBridgeManager.TERMUX_HOME, "retui-files", resolved);
            return "Termux bridge listing files: " + resolved;
        }

        if ("-ls".equals(option)) {
            TermuxBridgeManager.dispatchShell(info.context, "ls " + resolved, LIST_ALL_SCRIPT, TermuxBridgeManager.TERMUX_HOME, "retui-ls", resolved);
            return "Termux bridge listing: " + resolved;
        }

        return info.res.getString(helpRes());
    }

    private String localStatus(MainPack info) {
        TermuxBridgeManager.BridgeStatus status = TermuxBridgeManager.status(info.context);
        StringBuilder builder = new StringBuilder();
        builder.append("Re:T-UI Termux Bridge").append('\n');
        builder.append("termux: ").append(status.termuxInstalled ? "installed" : "missing").append('\n');
        builder.append("RUN_COMMAND declared: ").append(status.runCommandDeclared ? "yes" : "no").append('\n');
        builder.append("RUN_COMMAND granted: ").append(status.runCommandGranted ? "yes" : "no").append('\n');
        builder.append("current path: ").append(info.currentDirectory.getAbsolutePath()).append('\n');
        builder.append("probe: tbridge -probe");
        return builder.toString();
    }

    private boolean ensureReady(MainPack info) {
        TermuxBridgeManager.BridgeStatus status = TermuxBridgeManager.status(info.context);
        if (!status.termuxInstalled) {
            ohi.andre.consolelauncher.tuils.Tuils.sendOutput(info.context, "Termux is not installed.");
            return false;
        }
        if (!status.runCommandDeclared) {
            ohi.andre.consolelauncher.tuils.Tuils.sendOutput(info.context, "This Termux build does not expose RUN_COMMAND.");
            return false;
        }
        if (!status.runCommandGranted) {
            TermuxBridgeManager.requestRunCommandPermissionIfPossible(info.context);
            ohi.andre.consolelauncher.tuils.Tuils.sendOutput(info.context, "Grant Re:T-UI the Termux RUN_COMMAND permission, then retry.");
            ohi.andre.consolelauncher.tuils.Tuils.sendOutput(info.context, "Termux must also set allow-external-apps=true.");
            return false;
        }
        return true;
    }

    private String resolvePath(MainPack info, String path) {
        if (path == null || path.length() == 0) {
            return info.currentDirectory.getAbsolutePath();
        }
        File file = path.startsWith(File.separator) ? new File(path) : new File(info.currentDirectory, path);
        return file.getAbsolutePath();
    }

    private String setupText() {
        return "Termux bridge setup:\n"
                + "1. Install Termux.\n"
                + "2. In Termux run: termux-setup-storage\n"
                + "3. In Termux run: mkdir -p ~/.termux && echo allow-external-apps=true >> ~/.termux/termux.properties\n"
                + "4. Restart Termux.\n"
                + "5. Grant Re:T-UI the RUN_COMMAND permission from Android app settings.\n"
                + "6. Run: tbridge -probe";
    }

    @Override
    public int helpRes() {
        return R.string.help_tbridge;
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
        return pack.context.getString(helpRes());
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int index) {
        return pack.context.getString(helpRes());
    }
}
