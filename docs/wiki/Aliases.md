# Aliases

Aliases are one of the most important quality-of-life features in Re:T-UI.

They let you turn a longer command sequence into a short command that feels natural to type every day.

## Why Aliases Matter

Aliases keep the launcher command-first without forcing you to retype everything the hard way.

They are useful for:

- shortening repetitive commands
- creating your own vocabulary for common actions
- keeping roleplay flavor in the launcher
- building a more personal prompt-and-command experience

## Main Commands

- `alias -add <name> <command content>`
- `alias -rm <name>`
- `alias -ls`
- `alias -file`

## Examples

### Open a favorite app quickly

`alias -add yt open "YouTube Music"`

Now you can just type:

`yt`

### Save a common search flow

`alias -add bughunt search -gg Re-T-UI notifications`

Now:

`bughunt`

### Make the launcher feel more personal

`alias -add home apps -ls`

Now:

`home`

## Where Aliases Live

Aliases are stored in the launcher alias file and can also be opened directly with:

`alias -file`

This keeps them visible and editable for power users.

## How They Fit Re:T-UI

Aliases are part of what makes Re:T-UI feel like a terminal you live in instead of a command shell you visit occasionally.

They let users:

- shape the launcher around their habits
- hide complexity behind memorable words
- stay in the command flow instead of reaching for UI every time

## Recommended Alias Style

The best aliases are:

- short
- obvious to you
- tied to a real repeated action

Good examples:

- `yt`
- `mail`
- `favapps`
- `work`
- `night`

Less useful aliases are the ones you invent once and never remember again.

## Suggested Workflow

1. Notice a command you type often
2. Turn it into an alias
3. Use `alias -ls` occasionally to clean house
4. Remove aliases that no longer match how you use the launcher

## Future Possibilities

Aliases are also a natural place for future interactive flows, like confirmation prompts or protected commands.

That is not the main user flow today, but aliases are already the right conceptual home for that kind of power-user behavior.
