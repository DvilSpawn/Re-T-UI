# Re:TUI 2 - Build 416

Build 416 fixes preset exports that dropped decimal status-row positions such as `storage_index=2.1`.

## Changes

- Preserved decimal status pane indexes in saved presets, shareable configurations, imports, and preset application.
- Kept normal integer validation unchanged for non-status settings.

## Validation

- Preset manager regression test passed.
- Play Store unit tests, lint, APK assembly, and AAB bundle were run for this tagged source.

Version 2, Play Store version code 416.
