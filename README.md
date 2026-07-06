# AeternumShop

[![PaperMC 26.1.2](https://img.shields.io/badge/PaperMC-26.1.2-004ee9?logo=minecraft&logoColor=white)](https://papermc.io/)
[![Java 25](https://img.shields.io/badge/Java-25-e76f00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-c71a36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-2ea44f)](LICENSE)

> A modular, high-performance shop system for PaperMC 26.1.2 (Minecraft Java 26.1.2).
> Supports system trading, player-to-player listings, and a persistent mailbox — all backed by a flexible currency compression engine.

---

## Table of Contents

[Features](#features) | [Installation](#installation) | [Configuration](#configuration) | [Commands & Permissions](#commands--permissions) | [Currency System](#currency-system) | [Developer API](#developer-api) | [Building from Source](#building-from-source) | [Project Structure](#project-structure) | [Security](#security) | [Contributing](#contributing) | [License](#license)

---

## Features

| Feature | Description |
|---------|-------------|
| **System Shop** | Sell items to the server (`buy`) or purchase items from the server (`sell`) at configurable prices. |
| **Player Trading** | List items from your main hand for a custom price. Other players can browse and purchase them instantly. |
| **Mailbox** | Expired listings, cancelled trades, and offline earnings are safely stored in a per-player mailbox for later retrieval. |
| **Currency Compression** | Automatically compresses currency into higher-value bundles (e.g., 9 Diamonds → 1 Diamond Block) to minimize inventory clutter. |
| **Dual Database** | SQLite (default, zero-config) or MySQL for large-scale servers — both powered by HikariCP. |
| **MiniMessage Formatting** | Rich, colorful messages using Adventure's MiniMessage syntax (`<green>`, `<bold>`, gradients, etc.). |
| **Public API** | Third-party plugins can hook into the currency system and listen to trade events. |

---

## Installation

### Requirements

- **Server**: PaperMC 26.1.2 (Minecraft Java 26.1.2)
- **Java**: OpenJDK 25 or compatible
- **Build Tool**: Maven 3.9+ (if building from source)

### Quick Setup

1. Download the latest shaded JAR from [Releases](../../releases) (or build it yourself — see [Building from Source](#building-from-source)).
2. Place `aeternum-shop-1.0.0-SNAPSHOT.jar` into your server's `plugins/` directory.
3. Start or restart your server.
4. Edit the generated configuration files in `plugins/AeternumShop/` to your liking.
5. Run `/shop reload` to apply changes without a restart.

> **Note**: On first startup, the plugin creates default configs and initializes a local SQLite database. No external database setup is required unless you explicitly switch to MySQL.

---

## Configuration

All configuration files are located in `plugins/AeternumShop/` and are automatically merged with JAR defaults on load (missing keys are added safely).

### File Overview

| File | Purpose |
|------|---------|
| `config.yml` | Database type, player-trade limits, GUI rows, feature toggles, and MySQL credentials. |
| `currency.yml` | Base currency material, display name, and currency bundle definitions. |
| `prices.yml` | System shop pricing. `buy.<item>` = player sells to server; `sell.<item>` = player buys from server. |
| `messages.yml` | All player-facing text with MiniMessage formatting and `%key%` placeholders. |
| `gui.yml` | Materials, names, lore, and slot assignments for every GUI icon. |
| `plugin.yml` | Plugin metadata, command aliases, and permission nodes. |

### Key Config Notes

- **Database**: Set `database.type` to `SQLITE` (default) or `MYSQL`. Invalid values gracefully fall back to SQLite.
- **SQLite**: Uses WAL mode with a HikariCP pool size of 1 for safe concurrent access.
- **Currency**: Bundles must reference the exact `base-unit` ID. Currency is identified **by Material only** — NBT, display names, and lore are ignored by design.
- **Trade Limits**: `player-trade.listing-duration-hours` (default: 24) controls how long player listings remain active before expiring to the mailbox.

### Example: `prices.yml`

```yaml
buy:
  COBBLESTONE:
    amount: 64
    reward: 1
sell:
  DIAMOND:
    amount: 1
    cost: 4
```

> In this example, players can sell 64 Cobblestone for 1 base currency unit, or buy 1 Diamond from the server for 4 base currency units.

---

## Commands & Permissions

### Player Commands

| Command | Permission | Default | Description |
|---------|------------|---------|-------------|
| `/shop` | `aeternumshop.command.shop` | `true` | Opens the main shop GUI. |
| `/shop history` | `aeternumshop.command.history` | `true` | View your personal trade history. |
| `/shop mailbox` | `aeternumshop.command.mailbox` | `true` | Open your mailbox to claim expired listings and earnings. |
| `/shop add <price>` | `aeternumshop.command.add` | `true` | List the item in your main hand for the specified price. |
| `/shop show` | `aeternumshop.command.show` | `true` | Display your current active listings. |

### Admin Commands

| Command | Permission | Default | Description |
|---------|------------|---------|-------------|
| `/shop reload` | `aeternumshop.command.reload` | `op` | Reloads all YAML configs and currency definitions without restarting. |

### Permission Nodes

- `aeternumshop.command.shop` — Access the shop GUI.
- `aeternumshop.command.history` — View trade history.
- `aeternumshop.command.mailbox` — Access the mailbox.
- `aeternumshop.command.add` — Create player listings.
- `aeternumshop.command.show` — View own listings.
- `aeternumshop.command.reload` — Reload plugin configuration.
- `aeternumshop.admin` — Grants administrative access (implicitly includes reload).

---

## Currency System

AeternumShop uses a **material-based currency** with automatic compression and decomposition.

### How It Works

1. **Base Currency**: Defined in `currency.yml` (default: `DIAMOND`). This is the smallest unit of value.
2. **Currency Bundles**: Higher-value items that represent multiples of the base currency (default: `DIAMOND_BLOCK` = 9 base units).
3. **Compression**: When a player receives currency, the system automatically converts loose base units into bundles to save inventory space.
4. **Decompression**: When a player spends currency, bundles are broken down into base units as needed.

> **Important**: Because currency is identified by material only, any attached NBT (custom names, lore, enchantments) on currency items will be lost during compression. This is intentional to prevent item duplication exploits.

---

## Developer API

Third-party plugins can integrate with AeternumShop via the public API surface.

### Accessing the API

```java
import net.sakurain.mc.shop.api.ShopAPI;
import net.sakurain.mc.shop.api.events.TradeListener;

// Get the currency manager
var currencyManager = ShopAPI.getCurrencyManager();

// Check a player's balance
long balance = ShopAPI.getBalance(player);

// Economy operations
boolean withdrawn = ShopAPI.withdraw(player, 100L);
boolean deposited = ShopAPI.deposit(player, 50L);

// Listen to trade events
ShopAPI.registerTradeListener(new TradeListener() {
    @Override
    public void onPlayerTrade(Player buyer, Player seller, PlayerListing listing) {
        // Your custom logic here
    }
});
```

### Maven Dependency

```xml
<dependency>
    <groupId>net.sakurain.mc</groupId>
    <artifactId>aeternum-shop</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

> Ensure the AeternumShop JAR is present in your server's `plugins/` folder at runtime.

---

## Building from Source

```bash
# Clone the repository
git clone https://github.com/IYeaSakura/AeternumShop.git
cd AeternumShop

# Build the shaded JAR
mvn clean package

# Or skip tests
mvn clean package -DskipTests
```

The output artifact is located at:

```
target/aeternum-shop-1.0.0-SNAPSHOT.jar
```

Copy this file to your Paper server's `plugins/` directory.

### Tech Stack

| Layer | Technology |
|-------|------------|
| Platform | PaperMC 26.1.2 |
| Language | Java 25 |
| Build Tool | Maven 3.9+ |
| Core API | `io.papermc.paper:paper-api` (provided) |
| Connection Pool | HikariCP 5.1.0 (shaded) |
| MySQL Driver | MySQL Connector/J 8.4.0 (shaded) |
| Text Formatting | Adventure MiniMessage (provided by Paper) |

> HikariCP is relocated to `net.sakurain.mc.libs.hikari` at build time to avoid classpath conflicts with other plugins.

---

## Project Structure

```
AeternumShop/
├── pom.xml                           # Maven build configuration
├── src/main/java/net/sakurain/mc/shop/
│   ├── AeternumShop.java             # Plugin main class
│   ├── api/                          # Public API & events
│   ├── command/                      # Command executors & tab completion
│   ├── config/                       # YAML config & message loaders
│   ├── currency/                     # Currency compression engine
│   ├── database/                     # DAO layer (SQLite & MySQL)
│   ├── gui/                          # Inventory GUI implementations
│   ├── listener/                     # Bukkit event listeners
│   ├── model/                        # Data models & enums
│   ├── task/                         # Background scheduled tasks
│   ├── transaction/                  # Trade business logic
│   └── util/                         # Inventory, item, string, time helpers
└── src/main/resources/
    ├── plugin.yml
    ├── config.yml
    ├── currency.yml
    ├── gui.yml
    ├── messages.yml
    └── prices.yml
```

---

## Security

- **SQL Injection Prevention**: All database queries use `PreparedStatement`. No user input is ever concatenated into raw SQL.
- **Permission Enforcement**: Every admin action and cross-player GUI interaction verifies UUID ownership and permission nodes.
- **Inventory Safety**: All item and currency modifications run on the main server thread. Async operations are restricted to database I/O only.
- **Path Traversal Protection**: File access is strictly scoped to `plugin.getDataFolder()`. No arbitrary path resolution is permitted.
- **Credential Privacy**: MySQL credentials in `config.yml` are never logged to the console.
- **NMS-Free**: The plugin does not use net-minecraft-server (NMS) or reflection, ensuring maximum forward compatibility within the Paper 26.x ecosystem.

---

## Contributing

Contributions are welcome! Please follow these guidelines:

1. **Java 25 Syntax**: Feel free to use pattern matching for `instanceof`, `switch` expressions, `var` inference, and other modern features.
2. **Code Style**: Use `try-with-resources` for all JDBC resources. Prefer `Optional` for single-row DAO queries. Keep class/method/field names in English.
3. **Comments**: Use descriptive Chinese inline comments **only** when explaining non-obvious business logic; all identifiers must remain in English.
4. **Testing**: If adding tests, use JUnit 5 and MockBukkit (or a similar Paper testing framework). Update `pom.xml` accordingly.
5. **No NMS**: Avoid adding NMS or reflection unless absolutely necessary. If required, use Mojang mappings as per Paper 26.1.2 standards.

---

## License

This project is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.
