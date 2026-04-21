package ohi.andre.consolelauncher.managers;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.managers.xml.options.Theme;
import ohi.andre.consolelauncher.tuils.Tuils;

/**
 * Created by francescoandreuzzi on 26/07/2017.
 */

public class TimeManager {

    private static final Pattern COLOR_PATTERN = Pattern.compile("#(?:\\d|[a-fA-F]){6}");
    private static final Pattern SIZE_PATTERN = Pattern.compile("\\[size=(\\d+)](.*?)\\[/size]", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private FormatEntry[] outputDateFormatList;
    private FormatEntry[] statusDateFormatList;

    public static Pattern extractor = Pattern.compile("%t([0-9]*)", Pattern.CASE_INSENSITIVE);

    public static TimeManager instance;

    public TimeManager(Context context) {
        instance = this;
        String separator = XMLPrefsManager.get(Behavior.time_format_separator);

        outputDateFormatList = createList(context, XMLPrefsManager.get(Behavior.output_time_format), separator);
        statusDateFormatList = createList(context, XMLPrefsManager.get(Behavior.status_time_format), separator);
    }

    private FormatEntry[] createList(Context context, String format, String separator) {
        String[] formats = format.split(separator);
        FormatEntry[] list = new FormatEntry[formats.length];

        for(int c = 0; c < list.length; c++) {
            try {
                formats[c] = Tuils.patternNewline.matcher(formats[c]).replaceAll(Tuils.NEWLINE);

                int color = XMLPrefsManager.getColor(Theme.time_color);
                Matcher m = COLOR_PATTERN.matcher(formats[c]);
                if(m.find()) {
                    color = Color.parseColor(m.group());
                    formats[c] = m.replaceAll(Tuils.EMPTYSTRING);
                }

                list[c] = buildEntry(color, formats[c]);
            } catch (Exception e) {
                Tuils.sendOutput(Color.RED, context,"Invalid time format: " + formats[c]);
                if (c > 0) list[c] = list[0];
                else list[c] = buildFallbackEntry();
            }
        }
        return list;
    }

    private FormatEntry buildEntry(int color, String rawFormat) {
        List<FormatSegment> segments = new ArrayList<>();
        Matcher matcher = SIZE_PATTERN.matcher(rawFormat);
        int cursor = 0;
        boolean foundSizedSegment = false;

        while (matcher.find()) {
            foundSizedSegment = true;

            if (matcher.start() > cursor) {
                addSegment(segments, rawFormat.substring(cursor, matcher.start()), null);
            }

            addSegment(segments, matcher.group(2), Integer.parseInt(matcher.group(1)));
            cursor = matcher.end();
        }

        if (cursor < rawFormat.length()) {
            addSegment(segments, rawFormat.substring(cursor), null);
        }

        if (!foundSizedSegment || segments.isEmpty()) {
            segments.clear();
            addSegment(segments, rawFormat, null);
        }

        return new FormatEntry(color, segments);
    }

    private void addSegment(List<FormatSegment> segments, String pattern, Integer explicitSize) {
        if (pattern == null || pattern.length() == 0) {
            return;
        }

        segments.add(new FormatSegment(new SimpleDateFormat(pattern), explicitSize));
    }

    private FormatEntry buildFallbackEntry() {
        List<FormatSegment> segments = new ArrayList<>();
        segments.add(new FormatSegment(new SimpleDateFormat("HH:mm:ss"), null));
        return new FormatEntry(Color.RED, segments);
    }

    private FormatEntry get(int index, boolean isStatus) {
        FormatEntry[] list = isStatus ? statusDateFormatList : outputDateFormatList;
        if(list == null || list.length == 0) return null;
        if(index < 0 || index >= list.length) index = 0;

        return list[index];
    }

    public CharSequence replace(CharSequence cs) {
        return replace(null, Integer.MAX_VALUE, cs, -1, TerminalManager.NO_COLOR, false);
    }

    public CharSequence replace(CharSequence cs, int color) {
        return replace(null, Integer.MAX_VALUE, cs, -1, color, false);
    }

    public CharSequence replace(CharSequence cs, long tm, int color) {
        return replace(null, Integer.MAX_VALUE, cs, tm, color, false);
    }

    public CharSequence replace(CharSequence cs, long tm) {
        return replace(null, Integer.MAX_VALUE, cs, tm, TerminalManager.NO_COLOR, false);
    }

    public CharSequence replace(Context context, int size, CharSequence cs) {
        return replace(context, size, cs, -1, TerminalManager.NO_COLOR, false);
    }

    public CharSequence replace(Context context, int size, CharSequence cs, int color) {
        return replace(context, size, cs, -1, color, false);
    }

    public CharSequence replace(Context context, int size, CharSequence cs, long tm, int color, boolean isStatus) {
        if(tm == -1) {
            tm = System.currentTimeMillis();
        }

        if(cs instanceof String) {
            Tuils.log(Thread.currentThread().getStackTrace());
            Tuils.log("cant span a string!", cs.toString());
        }

        Date date = new Date(tm);

        Matcher matcher = extractor.matcher(cs);
        while(matcher.find()) {
            String number = matcher.group(1);
            if(number == null || number.length() == 0) number = "0";

            FormatEntry entry = get(Integer.parseInt(number), isStatus);
            if(entry == null) continue;

            CharSequence s = span(context, entry, color, date, size);
            cs = TextUtils.replace(cs, new String[] {matcher.group(0)}, new CharSequence[] {s});
        }

        FormatEntry entry = get(0, isStatus);
        cs = TextUtils.replace(cs, new String[] {"%t"}, new CharSequence[] {span(context, entry, color, date, size)});

        return cs;
    }

    public CharSequence getCharSequence(String s) {
        return getCharSequence(null, Integer.MAX_VALUE, s, -1, TerminalManager.NO_COLOR, true);
    }

    public CharSequence getCharSequence(String s, int color) {
        return getCharSequence(null, Integer.MAX_VALUE, s, -1, color, true);
    }

    public CharSequence getCharSequence(String s, long tm, int color) {
        return getCharSequence(null, Integer.MAX_VALUE, s, tm, color, true);
    }

    public CharSequence getCharSequence(String s, long tm) {
        return getCharSequence(null, Integer.MAX_VALUE, s, tm, TerminalManager.NO_COLOR, true);
    }

    public CharSequence getCharSequence(Context context, int size, String s) {
        return getCharSequence(context, size, s, -1, TerminalManager.NO_COLOR, true);
    }

    public CharSequence getCharSequence(Context context, int size, String s, int color) {
        return getCharSequence(context, size, s, -1, color, true);
    }

    public CharSequence getCharSequence(Context context, int size, String s, long tm, int color, boolean isStatus) {
        if(tm == -1) {
            tm = System.currentTimeMillis();
        }

        Date date = new Date(tm);

        Matcher matcher = extractor.matcher(s);
        if(matcher.find()) {
            String number = matcher.group(1);
            if(number == null || number.length() == 0) number = "0";

            FormatEntry entry = get(Integer.parseInt(number), isStatus);
            if(entry == null) {
                return null;
            }

            return span(context, entry, color, date, size);
        } else return null;
    }

    private CharSequence span(Context context, FormatEntry entry, int color, Date date, int size) {
        if(entry == null) return Tuils.EMPTYSTRING;

        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (FormatSegment segment : entry.segments) {
            if (segment == null || segment.formatter == null) {
                continue;
            }

            int start = builder.length();
            builder.append(segment.formatter.format(date));
            int end = builder.length();

            if (end <= start) {
                continue;
            }

            int segmentSize = segment.explicitSize != null ? segment.explicitSize : size;
            if (segmentSize != Integer.MAX_VALUE && context != null) {
                builder.setSpan(new AbsoluteSizeSpan(Tuils.convertSpToPixels(segmentSize, context)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        if (builder.length() == 0) {
            return Tuils.EMPTYSTRING;
        }

        int clr = color != TerminalManager.NO_COLOR ? color : entry.color;
        builder.setSpan(new ForegroundColorSpan(clr), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    public void dispose() {
        outputDateFormatList = null;
        statusDateFormatList = null;

        instance = null;
    }

    private static class FormatEntry {
        final int color;
        final List<FormatSegment> segments;

        FormatEntry(int color, List<FormatSegment> segments) {
            this.color = color;
            this.segments = segments;
        }
    }

    private static class FormatSegment {
        final SimpleDateFormat formatter;
        final Integer explicitSize;

        FormatSegment(SimpleDateFormat formatter, Integer explicitSize) {
            this.formatter = formatter;
            this.explicitSize = explicitSize;
        }
    }
}
