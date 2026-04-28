package ohi.andre.consolelauncher.tuils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.managers.settings.AppearanceSettings;

/**
 * Centralizes the styling of TUI widgets (Music, Notifications, etc.)
 * to ensure visual consistency and reduce UIManager bloat.
 */
public class TuiWidgetDecorator {

    public static void decorateWidget(View widgetRoot, int borderViewId, int labelViewId) {
        decorateWidget(widgetRoot, borderViewId, labelViewId,
                AppearanceSettings.musicWidgetBorderColor(),
                AppearanceSettings.musicWidgetTextColor());
    }

    public static void decorateWidget(View widgetRoot, int borderViewId, int labelViewId, int borderColor, int textColor) {
        if (widgetRoot == null) return;

        Context context = widgetRoot.getContext();
        int widgetBgColor = AppearanceSettings.terminalWindowBackground();
        int labelMaskColor = ColorUtils.setAlphaComponent(widgetBgColor, 255);
        boolean useDashed = AppearanceSettings.dashedBorders();

        // 1. Decorate Border
        View borderView = widgetRoot.findViewById(borderViewId);
        if (borderView != null) {
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.RECTANGLE);
            if (useDashed) {
                gd.setStroke((int) Tuils.dpToPx(context, 1.5f), borderColor,
                        Tuils.dpToPx(context, AppearanceSettings.dashLength()),
                        Tuils.dpToPx(context, AppearanceSettings.dashGap()));
            } else {
                gd.setStroke((int) Tuils.dpToPx(context, 1.5f), borderColor);
            }
            gd.setColor(widgetBgColor);
            borderView.setBackground(gd);
        }

        // 2. Decorate Label
        TextView widgetLabel = widgetRoot.findViewById(labelViewId);
        if (widgetLabel != null) {
            widgetLabel.setTextColor(textColor);
            widgetLabel.setTypeface(Tuils.getTypeface(context), Typeface.BOLD);
            try {
                GradientDrawable gd = (GradientDrawable) ResourcesCompat.getDrawable(
                        context.getResources(), R.drawable.apps_drawer_header_border, null);
                if (gd != null) {
                    gd = (GradientDrawable) gd.mutate();
                    if (useDashed) {
                        gd.setStroke((int) Tuils.dpToPx(context, 1.5f), borderColor,
                                Tuils.dpToPx(context, AppearanceSettings.dashLength()),
                                Tuils.dpToPx(context, AppearanceSettings.dashGap()));
                    } else {
                        gd.setStroke((int) Tuils.dpToPx(context, 1.5f), borderColor);
                    }
                    gd.setColor(labelMaskColor);
                    widgetLabel.setBackground(gd);
                }
            } catch (Exception ignored) {}
        }
    }

    public static GradientDrawable getRowBackground(Context context) {
        return getRowBackground(context, AppearanceSettings.notificationWidgetBorderColor());
    }

    public static GradientDrawable getRowBackground(Context context, int borderColor) {
        int widgetBgColor = AppearanceSettings.terminalWindowBackground();
        int rowBackground = ColorUtils.blendARGB(widgetBgColor, Color.BLACK, 0.22f);
        int strokeColor = ColorUtils.setAlphaComponent(borderColor, 140);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(Tuils.dpToPx(context, 4));
        bg.setColor(rowBackground);
        bg.setStroke((int) Tuils.dpToPx(context, 1.2f), strokeColor);
        return bg;
    }
}
