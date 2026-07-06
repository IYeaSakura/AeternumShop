# AeternumShop

A modular shop plugin for **PaperMC 26.1.2** (Minecraft Java 26.1.2). Supports system buy/sell, player-to-player trading, mailbox delivery, and a multi-tier currency system with automatic compression/decomposition.

## Features

- **System Shop**
  - System buy: sell configured items to the server for currency.
  - System sell: buy configured items from the server with currency.
  - Left-click for single transaction, right-click for maximum amount.
- **Player Trading**
  - List items from your main hand with `/shop add <price>`.
  - Browse active listings and buy from other players.
  - Cancel your own listings directly from the trading GUI.
  - Listings expire after a configurable duration (default 24 hours) and return to the seller's mailbox.
- **Mailbox**
  - Receive returned items and overflow currency.
  - Currency entries in the mailbox are automatically merged into the smallest number of entries.
- **Currency System**
  - Configurable base currency (e.g., diamond).
  - Configurable currency bundles (e.g., diamond block = 9 diamonds).
  - Automatic compression to the fewest inventory slots after every transaction.
  - Overflow currency goes to the mailbox when inventory is full.
- **Other**
  - Client-language item names in GUIs.
  - Actual stack amounts displayed on GUI items.
  - `/shop show` to inspect currency definitions and your inventory value.
  - `/shop reload` for operators to reload configuration.

## Requirements

- **Server**: Paper 26.1.2 (or compatible fork)
- **Java**: 25+
- **Build tool**: Maven 3.9+

## Installation

1. Download `aeternum-shop-1.0.0-SNAPSHOT.jar` from the `target/` directory (or build from source).
2. Place the jar in your server's `plugins/` folder.
3. Start the server. Default configuration files will be generated in `plugins/AeternumShop/`.
4. Edit `prices.yml`, `currency.yml`, `messages.yml`, and `gui.yml` as needed.
5. Run `/shop reload` or restart the server to apply changes.

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/shop` | `aeternumshop.command.shop` | Open the main shop menu. |
| `/shop history` | `aeternumshop.command.history` | Open your trade history. |
| `/shop mailbox` | `aeternumshop.command.mailbox` | Open your mailbox. |
| `/shop add <price>` | `aeternumshop.command.add` | List the item in your main hand at the given price. |
| `/shop show` | `aeternumshop.command.show` | Show currency info and your inventory's equivalent base currency value. |
| `/shop reload` | `aeternumshop.command.reload` | Reload plugin configuration. |

## Permissions

- `aeternumshop.command.shop` (default: true)
- `aeternumshop.command.add` (default: true)
- `aeternumshop.command.history` (default: true)
- `aeternumshop.command.mailbox` (default: true)
- `aeternumshop.command.show` (default: true)
- `aeternumshop.command.reload` (default: op)
- `aeternumshop.admin` (default: op, grants reload permission)

## Configuration

### `config.yml`

General plugin settings.

```yaml
database:
  type: SQLITE          # SQLITE or MYSQL
  mysql:
    host: localhost
    port: 3306
    database: aeternum_shop
    username: root
    password: ''
    pool-size: 10

player-trade:
  max-listings-per-player: 5
  max-history-records: 20
  listing-duration-hours: 24

gui:
  rows: 6
  border-item: GRAY_STAINED_GLASS_PANE
  border-name: " "
```

### `currency.yml`

Define the base currency and optional bundles.

```yaml
currency:
  base:
    id: "diamond"
    material: DIAMOND
    display-name: "钻石"
  bundles:
    - id: "diamond_block"
      material: DIAMOND_BLOCK
      display-name: "钻石块"
      base-equivalent: 9
      base-unit: "diamond"
```

### `prices.yml`

Define system buy/sell prices.

```yaml
buy:
  diamond:
    amount: 1
    reward: 1
sell:
  diamond:
    amount: 1
    cost: 1
```

### `messages.yml`

All player-facing messages. Supports [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting.

### `gui.yml`

Configure GUI item materials, names, slots, and lore for the main menu and navigation buttons.

## Building from Source

```bash
# Make sure JAVA_HOME points to Java 25
export JAVA_HOME=/path/to/jdk-25
export PATH=$JAVA_HOME/bin:$PATH

mvn clean package
```

The shaded jar will be located at:

```
target/aeternum-shop-1.0.0-SNAPSHOT.jar
```

## Notes

- The plugin uses SQLite by default. MySQL support is available via HikariCP (shaded into the jar).
- Currency items are identified by material only. Any item matching the configured base/bundle material is treated as currency.
- When a listing expires or is cancelled by the seller, the item is returned to the seller's mailbox.
- If a seller is offline when their listing is purchased, the earnings are placed in their mailbox.

## License

This project is proprietary software created for the Aeternum server network.
