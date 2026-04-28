# Re-TUI TODO

This is a working backlog for issues and user requests that should be handled after the current timer/pomodoro/widget-page branch stabilizes.

## High Priority

- Brightness permission flow
  - Running brightness commands does not make Re-TUI appear in Android's "Modify system settings" allow-list.
  - Verify manifest permission, settings intent target, package/flavor visibility, and whether the command should deep-link to `ACTION_MANAGE_WRITE_SETTINGS`.

- Package name / side-by-side install
  - A user tried to sideload Re-TUI alongside the original TUI Launcher and Android would not allow it.
  - Confirm the active `applicationId` for all flavors and whether any flavor still uses the upstream package name.
  - Decide whether Play/F-Droid/debug builds should intentionally use separate package names.

- Themer navigation stacking
  - Themer/settings hub views appear overlaid on top of previous views.
  - If terminal backgrounds are transparent, previous screens remain visible beneath the current screen.
  - Current screen should own an opaque terminal surface while the outside overlay can still let wallpaper bleed through.

## Widget / Terminal Polish

- Independent border colors per element
  - Notification terminal currently links border color and text color.
  - Add separate customization options for widget border, widget label, row text, and row border where it makes sense.

- Widget label masking
  - The box attached to widget borders does not mask the underlying border line when backgrounds are transparent.
  - Labels should paint an opaque/surface-matched background behind the text and fully cover the border underneath.

- Music widget title copy
  - Music widget currently shows `NOW PLAYING` in the border label and repeats `Now Playing:` before the song title.
  - Change the song line prefix to `Title:`.

- Toolbar app drawer icon styling
  - The new app drawer/burger toolbar icon has a visible background while the other toolbar icons do not.
  - Make it visually match the existing toolbar buttons.

## Integrations

- Termux integration
  - Define the intended scope first: launch Termux, run a command/intent, expose Re-TUI commands to Termux, or sync config files.
  - Check current Android restrictions and Termux intent/API support before implementation.
