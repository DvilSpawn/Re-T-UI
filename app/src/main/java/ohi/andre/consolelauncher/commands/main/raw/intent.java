package ohi.andre.consolelauncher.commands.main.raw;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.tuils.Tuils;

public class intent implements CommandAbstraction {

    private enum Mode {
        VIEW,
        ACTIVITY,
        BROADCAST,
        URI,
        CHECK
    }

    @Override
    public String exec(ExecutePack pack) {
        String input = pack.getString();
        List<String> args = Tuils.splitArgs(input);
        if (args.isEmpty()) {
            return pack.context.getString(helpRes());
        }

        Mode mode = modeFor(args.remove(0));
        if (mode == null) {
            return pack.context.getString(R.string.output_invalidarg);
        }

        try {
            Intent built = buildIntent(mode, args);
            if (built == null) {
                return pack.context.getString(helpRes());
            }

            if (mode == Mode.CHECK) {
                return checkIntent(pack, built);
            }

            if (mode == Mode.BROADCAST) {
                if (built.getAction() == null) {
                    return "intent -broadcast requires -a <action>";
                }
                if (built.getPackage() == null && built.getComponent() == null && !args.contains("--unsafe-implicit")) {
                    return "intent -broadcast requires -p or -n. Add --unsafe-implicit to send a broad implicit broadcast.";
                }
                pack.context.sendBroadcast(built);
                return "Broadcast sent: " + built.getAction();
            }

            pack.context.startActivity(built);
            return Tuils.EMPTYSTRING;
        } catch (ActivityNotFoundException e) {
            return "No activity found for intent.";
        } catch (SecurityException e) {
            return "Intent blocked by Android security: " + e.getMessage();
        } catch (IllegalArgumentException | URISyntaxException e) {
            return "Invalid intent: " + e.getMessage();
        }
    }

    private Mode modeFor(String value) {
        if ("-view".equalsIgnoreCase(value)) return Mode.VIEW;
        if ("-activity".equalsIgnoreCase(value)) return Mode.ACTIVITY;
        if ("-broadcast".equalsIgnoreCase(value)) return Mode.BROADCAST;
        if ("-uri".equalsIgnoreCase(value)) return Mode.URI;
        if ("-check".equalsIgnoreCase(value)) return Mode.CHECK;
        return null;
    }

    private Intent buildIntent(Mode mode, List<String> args) throws URISyntaxException {
        if (mode == Mode.VIEW) {
            if (args.isEmpty()) return null;
            return new Intent(Intent.ACTION_VIEW, Uri.parse(args.get(0)));
        }

        if (mode == Mode.URI) {
            if (args.isEmpty()) return null;
            return Intent.parseUri(args.get(0), Intent.URI_INTENT_SCHEME);
        }

        Intent built = new Intent();
        if (mode == Mode.CHECK && !args.isEmpty()) {
            Mode nested = modeFor(args.get(0));
            if (nested != null && nested != Mode.CHECK) {
                args.remove(0);
                return buildIntent(nested, args);
            }
        }

        String data = null;
        String type = null;
        boolean hasPayload = false;

        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            if ("--unsafe-implicit".equals(arg)) {
                continue;
            }
            if ("-a".equals(arg)) {
                built.setAction(requiredValue(args, ++i, "-a"));
                hasPayload = true;
            } else if ("-d".equals(arg)) {
                data = requiredValue(args, ++i, "-d");
                hasPayload = true;
            } else if ("-t".equals(arg)) {
                type = requiredValue(args, ++i, "-t");
                hasPayload = true;
            } else if ("-p".equals(arg)) {
                built.setPackage(requiredValue(args, ++i, "-p"));
                hasPayload = true;
            } else if ("-n".equals(arg)) {
                built.setComponent(parseComponent(requiredValue(args, ++i, "-n")));
                hasPayload = true;
            } else if ("--es".equals(arg)) {
                built.putExtra(requiredValue(args, ++i, "--es key"), requiredValue(args, ++i, "--es value"));
                hasPayload = true;
            } else if ("--ei".equals(arg)) {
                built.putExtra(requiredValue(args, ++i, "--ei key"), Integer.parseInt(requiredValue(args, ++i, "--ei value")));
                hasPayload = true;
            } else if ("--ez".equals(arg)) {
                built.putExtra(requiredValue(args, ++i, "--ez key"), Boolean.parseBoolean(requiredValue(args, ++i, "--ez value")));
                hasPayload = true;
            } else {
                throw new IllegalArgumentException("unknown option " + arg);
            }
        }

        if (data != null && type != null) {
            built.setDataAndType(Uri.parse(data), type);
        } else if (data != null) {
            built.setData(Uri.parse(data));
        } else if (type != null) {
            built.setType(type);
        }

        return hasPayload ? built : null;
    }

    private String requiredValue(List<String> args, int index, String option) {
        if (index >= args.size()) {
            throw new IllegalArgumentException(option + " needs a value");
        }
        return args.get(index);
    }

    private ComponentName parseComponent(String value) {
        ComponentName component = ComponentName.unflattenFromString(value);
        if (component == null) {
            throw new IllegalArgumentException("invalid component " + value);
        }
        return component;
    }

    private String checkIntent(ExecutePack pack, Intent built) {
        PackageManager pm = pack.context.getPackageManager();
        List<ResolveInfo> infos = new ArrayList<>();
        infos.addAll(pm.queryIntentActivities(built, PackageManager.MATCH_DEFAULT_ONLY));
        if (built.getAction() != null) {
            infos.addAll(pm.queryBroadcastReceivers(built, PackageManager.MATCH_DEFAULT_ONLY));
        }

        if (infos.isEmpty()) {
            return "No handlers found. Android package visibility may hide some targets.";
        }

        StringBuilder out = new StringBuilder();
        for (ResolveInfo info : infos) {
            CharSequence label = info.loadLabel(pm);
            String name = label == null ? "Unknown" : label.toString();
            String pkg = info.activityInfo != null ? info.activityInfo.packageName
                    : info.serviceInfo != null ? info.serviceInfo.packageName : null;
            String cls = info.activityInfo != null ? info.activityInfo.name
                    : info.serviceInfo != null ? info.serviceInfo.name : null;
            out.append(name);
            if (pkg != null) out.append(" | ").append(pkg);
            if (cls != null) out.append("/").append(cls);
            out.append(Tuils.NEWLINE);
        }
        return out.toString().trim();
    }

    @Override
    public int[] argType() {
        return new int[] {CommandAbstraction.PLAIN_TEXT};
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public int helpRes() {
        return R.string.help_intent;
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int indexNotFound) {
        return pack.context.getString(helpRes());
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return pack.context.getString(helpRes());
    }
}
