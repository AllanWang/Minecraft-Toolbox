# Minecraft-Toolbox

A collection of QOL features for Minecraft.

# Features

## Compass

* Set target to follow player, point to bed/coordinate, or reset to spawn.
* Right click to show current coordinates; right click below feet to announce
  coordinates to all online users.
* When crouching, right click on land to create a "beacon". Beacon is a set of
  floating torches above the block, and will only be created if there's space.
  Crouch and right click again to delete. There were some considerations towards
  using actual beacons, but it seems like this one is the least intrusive.
  
## Teleport

* Teleport to player if other player is not in a vehicle (avoids kicking them out).
* Teleport to facing direction, upwards, or downwards, up to 5 tiles.

# Development

This repo comes with a DSL for building commands + dagger integration.
The `base` module is usable in other projects, and is separated from the
plugin's feature set.