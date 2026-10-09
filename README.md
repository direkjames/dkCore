# dkCore

Shared core plugin for the dk suite on our server. Author: **direk james**.
Server: **Purpur 26.3**, Java **25**. Package: `com.direk.dkcore`.

> **All Rights Reserved.** See [LICENSE](LICENSE). No one may use, copy or modify this code.

## Build

1. Open this folder in IntelliJ IDEA. Set Project SDK and Gradle JVM to **Java 25**.
2. `./gradlew build` → `build/libs/dkCore-1.1.1.jar` goes in the server's `plugins/` folder.
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
    compileOnly("com.direk:dkcore:1.1.1")
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
| `economy.EconomyService` | `balance`, `has`, `withdraw`, `deposit`, `format`. Get it with `economy().get()` (Vault today, dkBank later) |
| `gui.Menu` / `ItemBuilder` | Locked chest menus with click handlers |
| `afk.AfkService` / `DkAfkChangeEvent` | AFK hook for DirekAntiAFK |
| `util.TimeUtil` / `TimeFormatter` | Parse `1h30m`; format seconds from a config section |

## Commands

`/dkcore` status, `/dkcore reload` (permission `dkcore.admin`).

## Startup banner

On startup the console shows the dkCore logo, version, server, Java, database and startup time.
When the server finishes loading, a "dk suite ready" summary lists every dk plugin (OK / DISABLED),
the economy and the AFK provider. Turn it off with `startup-banner: false`.

## Config

`plugins/dkCore/config.yml`: database type (`sqlite` or `mysql`) and MySQL login.
On first start the server downloads HikariCP, SQLite and the MySQL driver, so it needs internet access.
