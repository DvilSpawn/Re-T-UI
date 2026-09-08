# Re:TUI V.2 - Build 417

Build 417 fixes preset exports that dropped custom terminal prompt prefixes such as `t-ui ~$`.

## Changes

- Preserved short terminal-style `input_prefix` and `input_root_prefix` values in saved presets, shareable configurations, imports, and preset application.
- Kept personal free-form UI text excluded from shareable preset data.

## Validation

- Preset manager regression test passed.
- Play Store unit tests, lint, APK assembly, and AAB bundle were run for this tagged source.

Version 2, Play Store version code 417.
