# dkCore

Shared core plugin for the dk suite on our server. Author: **direk james**.
Server: **Purpur 26.3**, Java **25**. Package: `com.direk.dkcore`.

> **All Rights Reserved.** See [LICENSE](LICENSE). No one may use, copy or modify this code.

## Build

0. dkCore compiles against dkBank's Developer API. In the **dkBank** project run
   `gradlew :api:publishToMavenLocal` once (and again whenever dkBank's API changes).
1. Open this folder in IntelliJ IDEA. Set Project SDK and Gradle JVM to **Java 25**.
2. `./gradlew build` → `build/libs/dkCore-1.3.0.jar` goes in the server's `plugins/` folder.
3. `./gradlew publishToMavenLocal` → lets other dk plugins compile against dkCore.

Run step 3 again every time dkCore changes and a plugin needs the new code.

## Using dkCore in another dk plugin

`build.gradle.kts`:
```kotlin
repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.purpurmc.org/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
}
dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:26.3.build.+")
    compileOnly("com.direk:dkcore:1.3.0")
}
```

`plugin.yml`:
```yaml
author: direk james
depend: [dkCore]
```

Main class extends `com.direk.dkcore.DkPlugin` (not JavaPlugin) and implements `enable()` / `disable()`.
Never shade dkCore into a plugin jar; it must stay `compileOnly`.

## API overview

| Class | What it gives you |
|---|---|
| `DkPlugin` | `cfg()` config.yml, `messages()` messages.yml, `tasks()`, `database()`, `economy()`, `core()`, `registerCommand()`, `listen()`, `reloadAll()`, `userConfigSections()` |
| `DkCore` | `DkCore.get()`, `players()`, `database()`, `economy()`, `isAfk(uuid)`, `hasAfkProvider()` |
| `config.ConfigFile` | YAML file that auto-adds new default keys; `userSections` stay as the owner edits them |
| `message.Messages` | MiniMessage with `<prefix>`; `send`, `actionBar`, `get`, `parse`; `Messages.text/styled/component` placeholders. Plugins need `no-permission`, `player-only`, `unknown-player` keys |
| `command.DkCommand` / `SubCommand` | Root command + sub commands with permissions, player-only and tab completion |
| `scheduler.Tasks` | `sync`, `later`, `async`, `timer`, `asyncTimer` (repeating tasks auto-cancel on disable) |
| `database.Database` | Shared SQLite/MySQL. `query`, `execute`, `createTables`, `upsert`. One ordered DB thread. Prefix your tables (`dkfly_`) |
| `player.PlayerService` | `dk_players` table; `find(name)` resolves online/offline players |
| `bank.BankService` | dkBank: `give`, `take`, `transfer`, `balance`, `cachedBalance`, `has`, `tier`, `tierName`, `rank`, `isLocked`, `formatPlain`. Get it with `bank().get()`; empty when dkBank isn't installed. Futures complete on the main thread |
| `economy.EconomyService` | `balance`, `has`, `withdraw`, `deposit`, `formatPlain` (use this to show money), `format` (raw, may contain colors). Get it with `economy().get()`. This is the **wallet** (Vault); the bank is `bank()` |
| `gui.Menu` / `ItemBuilder` | Locked chest menus with click handlers |
| `afk.AfkService` / `DkAfkChangeEvent` | AFK hook for DirekAntiAFK |
| `util.TimeUtil` / `TimeFormatter` | Parse `1h30m`; format seconds from a config section |
| `util.TextUtil` | `stripSection` / `stripColors`: remove color codes from other plugins' text |

## dkBank link

- dkBank is a standalone plugin and never depends on dkCore. dkCore lists it as a `softdepend`,
  so dkBank enables first; **dkBank must never list dkCore back** (that would be a loop).
- `bank()` gives every dk plugin dkBank's bank without needing dkBank on its own classpath.
- When an AFK provider (DirekAntiAFK) is registered, dkCore passes AFK status to dkBank, so AFK
  players earn the offline interest rate.
- The wallet (`economy()`, Vault) and the bank (`bank()`, dkBank) are separate on purpose.

## Rules for every dk plugin

- **Money:** always show amounts with `Messages.money("price", eco, amount)`,
  `eco.formatPlain(amount)` or `bank.formatPlain(amount)`, never `format(amount)`. Economy plugins may add their own
  color codes (e.g. `§0`), which would override the colors in our YAML files.
- **Outside text:** `Messages.text(...)` removes `§` color codes automatically, so values from
  other plugins (PlaceholderAPI, item names) can't recolor messages.
- **Author:** `author: direk james` in every `plugin.yml`.

## Commands

`/dkcore` status, `/dkcore reload` (permission `dkcore.admin`).

## Startup banner

On startup the console shows the dkCore logo, version, server, Java, database and startup time.
When the server finishes loading, a "dk suite ready" summary lists every dk plugin (OK / DISABLED),
the economy and the AFK provider. Turn it off with `startup-banner: false`.

## Config

`plugins/dkCore/config.yml`: database type (`sqlite` or `mysql`) and MySQL login.
On first start the server downloads HikariCP, SQLite and the MySQL driver, so it needs internet access.
