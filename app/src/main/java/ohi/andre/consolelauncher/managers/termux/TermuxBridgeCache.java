package ohi.andre.consolelauncher.managers.termux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TermuxBridgeCache {

    private static final long REQUEST_TTL_MS = 2500;
    private static final Map<String, Entry> dirs = new HashMap<>();
    private static final Map<String, Entry> files = new HashMap<>();
    private static final Map<String, Long> requests = new HashMap<>();

    public static synchronized void putDirs(String path, String stdout) {
        dirs.put(path, new Entry(parse(stdout)));
    }

    public static synchronized void putFiles(String path, String stdout) {
        files.put(path, new Entry(parse(stdout)));
    }

    public static synchronized List<String> dirs(String path) {
        Entry entry = dirs.get(path);
        return entry == null ? Collections.emptyList() : entry.values;
    }

    public static synchronized List<String> files(String path) {
        Entry entry = files.get(path);
        return entry == null ? Collections.emptyList() : entry.values;
    }

    public static synchronized boolean shouldRequest(String type, String path) {
        String key = type + ":" + path;
        long now = System.currentTimeMillis();
        Long last = requests.get(key);
        if (last != null && now - last < REQUEST_TTL_MS) {
            return false;
        }
        requests.put(key, now);
        return true;
    }

    private static List<String> parse(String stdout) {
        List<String> values = new ArrayList<>();
        if (stdout == null) {
            return values;
        }

        String[] lines = stdout.trim().split("\\n");
        for (String line : lines) {
            String value = line.trim();
            if (value.endsWith("/")) {
                value = value.substring(0, value.length() - 1);
            }
            if (value.length() > 0) {
                values.add(value);
            }
        }
        Collections.sort(values, String.CASE_INSENSITIVE_ORDER);
        return values;
    }

    private static class Entry {
        final List<String> values;

        Entry(List<String> values) {
            this.values = values;
        }
    }
}
