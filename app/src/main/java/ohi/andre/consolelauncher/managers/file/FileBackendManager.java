package ohi.andre.consolelauncher.managers.file;

import android.content.Context;

import ohi.andre.consolelauncher.managers.termux.TermuxBridgeManager;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;

public class FileBackendManager {

    public enum Mode {
        AUTO,
        NATIVE,
        TERMUX,
        OFF
    }

    public enum Active {
        NATIVE,
        TERMUX,
        OFF
    }

    public static Mode configuredMode() {
        String value = XMLPrefsManager.get(Behavior.file_backend);
        if (value == null) {
            return Mode.AUTO;
        }

        value = value.trim().toLowerCase();
        if ("native".equals(value)) {
            return Mode.NATIVE;
        }
        if ("termux".equals(value)) {
            return Mode.TERMUX;
        }
        if ("off".equals(value)) {
            return Mode.OFF;
        }
        return Mode.AUTO;
    }

    public static Active activeBackend(Context context) {
        Mode mode = configuredMode();
        if (mode == Mode.OFF) {
            return Active.OFF;
        }
        if (mode == Mode.NATIVE) {
            return Active.NATIVE;
        }
        if (mode == Mode.TERMUX) {
            return termuxReady(context) ? Active.TERMUX : Active.OFF;
        }
        return termuxReady(context) ? Active.TERMUX : Active.NATIVE;
    }

    public static boolean termuxReady(Context context) {
        TermuxBridgeManager.BridgeStatus status = TermuxBridgeManager.status(context);
        return status.termuxInstalled && status.runCommandDeclared && status.runCommandGranted;
    }

    public static String statusLine(Context context) {
        Mode mode = configuredMode();
        Active active = activeBackend(context);
        TermuxBridgeManager.BridgeStatus status = TermuxBridgeManager.status(context);
        return "file_backend=" + mode.name().toLowerCase()
                + "\nactive_backend=" + active.name().toLowerCase()
                + "\ntermux_installed=" + yesNo(status.termuxInstalled)
                + "\ntermux_run_command_declared=" + yesNo(status.runCommandDeclared)
                + "\ntermux_run_command_granted=" + yesNo(status.runCommandGranted);
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }
}
