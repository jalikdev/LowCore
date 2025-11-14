<h1 align="center">💡 LowCore</h1>

---

<h2 align="center">❗ Still in development ❗</h2>

<p align="center">
  <b>A lightweight and modular Minecraft plugin by <a href="https://github.com/jalikdev">jalikdev</a></b><br>
  Designed for modern Paper/Spigot servers with a focus on clean utilities and high performance.
</p>

---

## ✨ Features

### 🆕 Core Features (v1.3)
✅ **Cleanup GUI** – Remove items, mobs, vehicles & XP orbs with a clean interface  
✅ **Performance Monitor** – TPS, MSPT, RAM, chunks, players  
✅ **Vanish System** – Full vanish with fake join/leave messages  
✅ **MOTD System** – Custom server list message with placeholders  
✅ **Join/Quit Messages** – Fully configurable join/leave formatting

### 🧰 Utility Commands
✅ **/gm** – Change your gamemode  
✅ **/fly** – Toggle flight mode  
✅ **/ec** – Open your or another player's ender chest  
✅ **/invsee** – Fully live inventory viewing (armor, offhand, updates)  
✅ **/hat** – Put an item on your head  
✅ **/heal /feed** – Basic healing and feeding utilities  
✅ **/craft** – Open crafting table  
✅ **/anvil** – Open anvil GUI  
✅ **/repair** – Repair items  
✅ **/spawnmob** – Spawn mobs with autocompletion  
✅ **/killall** – Remove mobs globally, by type, or by radius  
✅ **/god** – Toggle invincibility  
✅ **/speed** – Control walk/fly speed  
✅ **/log** – View recent admin actions  
✅ **/lowcore** – Plugin info, reload, debug tools

---

## ⚙️ Setup

1. Download the latest release from the [**Releases**](https://github.com/jalikdev/LowCore/releases) page
2. Drop the `.jar` file into your server’s `/plugins` folder
3. Restart your server
4. Done! 🎉

---

## 🧩 Commands Overview

| Command        | Description                                   | Permission                                | Default |
|----------------|-----------------------------------------------|--------------------------------------------|---------|
| `/lowcore`     | Plugin info, reload, help                     | `lowcore.command`                           | op      |
| `/gm`          | Change gamemode                               | `lowcore.gm`                                | op      |
| `/fly`         | Toggle flight                                 | `lowcore.fly`                               | op      |
| `/ec`          | Open own/others ender chest                   | `lowcore.ec` / `lowcore.ec.others`          | op      |
| `/invsee`      | Live inventory view                           | `lowcore.invsee`                            | op      |
| `/hat`         | Put held item on head                         | `lowcore.hat` / `lowcore.hat.others`        | true/op |
| `/heal`        | Heal players                                  | `lowcore.heal` / `lowcore.heal.others`      | op      |
| `/feed`        | Feed players                                  | `lowcore.feed` / `lowcore.feed.others`      | op      |
| `/craft`       | Open crafting table                           | `lowcore.craft`                             | op      |
| `/anvil`       | Open anvil interface                          | `lowcore.anvil`                             | op      |
| `/repair`      | Repair held/all items                         | `lowcore.repair`                            | op      |
| `/spawnmob`    | Spawn mobs                                    | `lowcore.spawnmob`                          | op      |
| `/killall`     | Kill mobs globally/by type/radius             | `lowcore.killall`                           | op      |
| `/god`         | Toggle invincibility                          | `lowcore.god`                               | op      |
| `/speed`       | Set walk/fly speed                            | `lowcore.speed`                             | op      |
| `/cleanup`     | Lag cleanup GUI                               | `lowcore.cleanup`                           | op      |
| `/performance` | Show TPS/MSPT/RAM/chunks                      | `lowcore.performance`                       | op      |
| `/log`         | Show recent admin actions                     | `lowcore.log`                               | op      |
| `/vanish`      | Vanish + fake join/leave                      | `lowcore.vanish`                            | op      |

---

## 🔧 Config Features

### Included systems:
- ✔ Custom prefixes
- ✔ Join/Quit messages
- ✔ MOTD (two lines, placeholders: `%version%`, `%online%`, `%max%`)
- ✔ Vanish messages
- ✔ Lag cleanup GUI settings
- ✔ Performance options (MSPT, chunk counter)

---

## 🧱 Tech Stack

- ☕ Java **21+**
- 🔧 Paper / Spigot **1.21.1+**
- 🧩 Maven project
- 💻 Developed using IntelliJ IDEA

---

## 🧾 License

This project is licensed under the [MIT License](LICENSE).
