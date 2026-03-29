
# Project Title

A brief description of what this project does and who it's for

# 🌟 PathLight

> PathLight is a Minecraft plugin that lets you create illuminated paths to guide players between two destinations. With immersive particle effects, it makes in-game navigation more intuitive and visually appealing.
> A built-in API is also available, allowing developers to easily expand its functionality and create their own add-ons (see [💻 Developer API](#section-api)) .

![License](https://img.shields.io/github/license/Fabrielg/pathlight)
![Release](https://img.shields.io/github/v/release/Fabrielg/pathlight)
![Build](https://img.shields.io/github/actions/workflow/status/Fabrielg/pathlight/build.yml)
![Paper](https://img.shields.io/badge/Paper-1.21.4-blue)

---

## ✨ Features

- **Real-time particle trail** guiding players to their destination
- **A\* pathfinding** on a hand-crafted navigation graph
- **In-game editor tool** to place waypoints and connections without editing files
- **Fully configurable** colors, particle density, curve tension
- **Public API** for developers to integrate PathLight into their own plugins

---

## 📦 Installation

1. Download the latest `PathLight-x.x.x.jar` from the [Releases](https://github.com/TonPseudo/pathlight/releases) page
2. Drop it into your server's `plugins/` folder
3. Restart your server
4. Use `/pathtool` in-game to get the editor tool

---

## 📋 Commands

| Command | Permission | Description |
|---|---|---|
| `/path <destination>` | `pathlight.use` | Navigate to a destination |
| `/path cancel` | `pathlight.use` | Cancel current navigation |
| `/pathtool` | `pathlight.admin` | Get the editor tool |
| `/pathlight reload` | `pathlight.admin` | Reload config and graph |
| `/pathlight stats` | `pathlight.admin` | Graph statistics |
| `/pathlight list <locations\|waypoints>` | `pathlight.admin` | List all entries |
| `/pathlight info <waypoint\|location> <id>` | `pathlight.admin` | Entry details |
| `/pathlight delete <waypoint\|location> <id>` | `pathlight.admin` | Delete an entry |
| `/pathlight tp <location name>` | `pathlight.admin` | Teleport to a location |

---

## 🛠️ Editor Tool

Get the tool with `/pathtool` (requires `pathlight.admin` permission).

| Action | Result |
|---|---|
| Right click | Cycle modes (Waypoint → Location) |
| Shift + Right click | Toggle auto-edge ON/OFF |
| Left click on block | Place waypoint (+ auto-connect if enabled) |
| Left click on waypoint | Select as connection point |
| Shift + Left click | Cancel selection / Delete waypoint |

---

## ⚙️ Configuration
```yaml
particles:
  trail-style: CATMULL_ROM   # LINEAR or CATMULL_ROM
  trail-color: "255,140,0"   # R,G,B
  spacing: 0.5
  size: 1.0
  catmull-tension: 0.5
  catmull-samples-per-segment: 12

navigation:
  refresh-interval: 10       # ticks (20 = 1 second)
  off-path-threshold: 8.0    # blocks

editor:
  waypoint-click-radius: 3.0
  visualization-interval: 10
```

---

<details>
<summary>#<a name="section-1"></a> 💻 Developer API</summary>

Add the dependency to your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.fabrielg.PathLight</groupId>
    <artifactId>pathlight-api</artifactId>
    <version>v1.0.3</version> <!-- change to the desired version -->
    <scope>provided</scope>
</dependency>
```

Declare PathLight as a dependency in your `plugin.yml`:
```yaml
depend: [PathLight]
```

### Basic usage
```java
public class MyPlugin extends JavaPlugin {

    private PathLightAPI pathLight;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("PathLight") == null) {
            getLogger().warning("PathLight not found!");
            return;
        }
        this.pathLight = PathLightAPI.getInstance();
    }

    // Start navigation
    public void navigate(Player player, String destination) {
        boolean started = pathLight.startNavigation(player, destination);
        if (!started) {
            player.sendMessage("No path found to " + destination);
        }
    }

    // Change trail color dynamically
    public void setDanger(Player player) {
        IActiveTrail trail = pathLight.getActiveTrail(player);
        if (trail != null) {
            trail.setTrailColor(Color.RED);
        }
    }
}
```

### Events
```java
@EventHandler
public void onPathStart(PathStartEvent event) {
    // Cancellable - return false to prevent navigation
    event.getPlayer().sendTitle("Navigation started!", 
        "→ " + event.getDestination().getName(), 10, 60, 10);
}

@EventHandler
public void onPathEnd(PathEndEvent event) {
    if (event.getReason() == PathEndEvent.Reason.REACHED) {
        // Player arrived - trigger quest, reward, etc.
        giveReward(event.getPlayer());
    }
}

@EventHandler
public void onPathRecalculate(PathRecalculateEvent event) {
    event.getPlayer().sendMessage("Recalculating route...");
}
```
</details>

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.

---

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
