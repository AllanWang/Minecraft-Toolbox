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

* Teleport to player if other player is not in a vehicle (avoids kicking them
  out).
* Teleport to facing direction, upwards, or downwards, up to 5 tiles.

## Terraforming

Terraforming works by forming a 2D path, and modifying blocks within that path,
up to a certain y axis range. A path is defined as a closed loop, where each
block in the path is adjacent to exactly 2 other blocks, and where the blocks
share the same y value. The adjacency restriction disallows crossovers in the
path, or edges that are next to another edge. To execute terraforms, a player
must stand above one of the blocks in the path. The ground level is defined as
the y plane below the path. Operations are as follows:

* Up - clear out blocks from ground upwards
* Down - clear out blocks from ground downwards
* Fill - add dirt blocks from ground downwards

# Development

This repo comes with a DSL for building commands + dagger integration.
The `base` module is usable in other projects, and is separated from the
plugin's feature set.