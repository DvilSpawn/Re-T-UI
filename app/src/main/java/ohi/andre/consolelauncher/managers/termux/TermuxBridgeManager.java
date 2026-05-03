package ohi.andre.consolelauncher.managers.termux;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import ohi.andre.consolelauncher.UIManager;

public class TermuxBridgeManager {

    public static final String TERMUX_PACKAGE = "com.termux";
    public static final String TERMUX_RUN_COMMAND_PERMISSION = "com.termux.permission.RUN_COMMAND";
    public static final String TERMUX_RUN_COMMAND_ACTION = "com.termux.RUN_COMMAND";
    public static final String TERMUX_RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService";
    public static final String TERMUX_RUN_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH";
    public static final String TERMUX_RUN_COMMAND_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS";
    public static final String TERMUX_RUN_COMMAND_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR";
    public static final String TERMUX_RUN_COMMAND_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND";
    public static final String TERMUX_RUN_COMMAND_PENDING_INTENT = "com.termux.RUN_COMMAND_PENDING_INTENT";

    public static final String TERMUX_HOME = "/data/data/com.termux/files/home";
    public static final String TERMUX_SH = "/data/data/com.termux/files/usr/bin/sh";
    public static final String RESULT_PREFIX = "retui-bridge:";

    public static boolean isTermuxInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo(TERMUX_PACKAGE, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean termuxDeclaresRunCommandPermission(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(TERMUX_PACKAGE, PackageManager.GET_PERMISSIONS);
            if (info.permissions != null) {
                for (PermissionInfo permission : info.permissions) {
                    if (permission != null && TERMUX_RUN_COMMAND_PERMISSION.equals(permission.name)) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return resolvesRunCommandService(context);
    }

    public static boolean hasRunCommandPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        PermissionInfo permission = findRunCommandPermission(context);
        return permission == null
                || ContextCompat.checkSelfPermission(context, TERMUX_RUN_COMMAND_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean requestRunCommandPermissionIfPossible(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !(context instanceof Activity)) {
            return false;
        }

        PermissionInfo permission = findRunCommandPermission(context);
        if (permission == null) {
            return false;
        }

        ActivityCompat.requestPermissions((Activity) context, new String[] {TERMUX_RUN_COMMAND_PERMISSION}, 8127);
        return true;
    }

    public static BridgeStatus status(Context context) {
        BridgeStatus status = new BridgeStatus();
        status.termuxInstalled = isTermuxInstalled(context);
        status.runCommandDeclared = status.termuxInstalled && termuxDeclaresRunCommandPermission(context);
        status.runCommandGranted = status.termuxInstalled && hasRunCommandPermission(context);
        return status;
    }

    public static boolean dispatchShell(Context context, String label, String script, String workDir, String... args) {
        Intent intent = new Intent(TERMUX_RUN_COMMAND_ACTION);
        intent.setClassName(TERMUX_PACKAGE, TERMUX_RUN_COMMAND_SERVICE);
        intent.putExtra(TERMUX_RUN_COMMAND_PATH, TERMUX_SH);
        intent.putExtra(TERMUX_RUN_COMMAND_WORKDIR, workDir == null ? TERMUX_HOME : workDir);
        intent.putExtra(TERMUX_RUN_COMMAND_BACKGROUND, true);
        intent.putExtra(TERMUX_RUN_COMMAND_PENDING_INTENT, createResultPendingIntent(context, label));

        String[] commandArgs = new String[args.length + 3];
        commandArgs[0] = "-c";
        commandArgs[1] = script;
        // sh -c uses the first extra argument as $0. Keep caller args starting at $1.
        commandArgs[2] = "retui";
        System.arraycopy(args, 0, commandArgs, 3, args.length);
        intent.putExtra(TERMUX_RUN_COMMAND_ARGUMENTS, commandArgs);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent);
        } else {
            context.startService(intent);
        }
        return true;
    }

    private static PendingIntent createResultPendingIntent(Context context, String label) {
        Intent resultIntent = new Intent(context, TermuxResultService.class);
        resultIntent.putExtra(UIManager.EXTRA_TERMUX_RESULT_PATH, RESULT_PREFIX + label);

        int flags = PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_MUTABLE;
        }

        return PendingIntent.getService(context, (int) System.currentTimeMillis(), resultIntent, flags);
    }

    private static PermissionInfo findRunCommandPermission(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(TERMUX_PACKAGE, PackageManager.GET_PERMISSIONS);
            if (info.permissions == null) {
                return null;
            }

            for (PermissionInfo permission : info.permissions) {
                if (permission != null && TERMUX_RUN_COMMAND_PERMISSION.equals(permission.name)) {
                    return permission;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static boolean resolvesRunCommandService(Context context) {
        Intent intent = new Intent(TERMUX_RUN_COMMAND_ACTION);
        intent.setClassName(TERMUX_PACKAGE, TERMUX_RUN_COMMAND_SERVICE);
        try {
            ResolveInfo info = context.getPackageManager().resolveService(intent, 0);
            return info != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static class BridgeStatus {
        public boolean termuxInstalled;
        public boolean runCommandDeclared;
        public boolean runCommandGranted;
    }
}
