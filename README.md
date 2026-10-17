# MobsBeGone (Unofficial 1.20.1 Fabric Backport)

An unofficial Fabric backport of [MobsBeGone](https://modrinth.com/mod/mobsbegone) by Okaeri Game Studio for Minecraft 1.20.1. This project is not affiliated with or endorsed by the original authors.

## What it does

The mod reads `config/mobsbegone-blacklist.json` when a server starts. Entity IDs in the JSON array are prevented from spawning. This applies to vanilla and modded entities when they are registered before the world starts.

The bundled default list blocks the vanilla living mobs available in Minecraft 1.20.1. Edit the list to choose which mobs to block; restart the world or server after changing it. The configuration format and file name match the upstream mod.

```json
[
  "minecraft:creeper",
  "minecraft:enderman",
  "anothermod:some_mob"
]
```

## Compatibility

- Minecraft 1.20.1
- Fabric Loader 0.16.14 or newer
- Fabric API 0.92.6+1.20.1, or a newer Fabric API build that still targets Minecraft 1.20.1
- Java 17
- Server-side; install it on the server for multiplayer or in the integrated server for singleplayer

## Source, license, and credits

The port is based on [Okaeri Game Studio's MobsBeGone source](https://github.com/OkaeriGameStudio/MobsBeGone), originally written by Olivia and EasyMochi. Upstream is licensed under the MIT License; the full copyright and permission notice is included in this project and in the built JAR. Port changes are documented in `PORT-NOTICE.md`.

Prepared with AI assistance.
