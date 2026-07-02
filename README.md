# Spear Debug Tool (Step 1 of the ViaBackwards Spear project)

## What this is
A tiny **Fabric 1.20.1 client-side mod**. It doesn't fix textures or add
spear mechanics yet — it's a diagnostic tool. We need it first because,
as a player without server access, we can't know in advance exactly which
vanilla item + `CustomModelData` value ViaBackwards is downgrading the
1.21.11 Spear into on this particular server. Different ViaBackwards
versions/configs can map it slightly differently.

## What it does
- Adds two keybinds (change them in Controls if they conflict):
  - **K** — dumps the item in your main hand
  - **L** — dumps the item in your off hand
- Prints the item's registry ID, display name, and full NBT (including
  `CustomModelData` if present) to chat and to `run/logs/latest.log`.

## How to build it (no local install needed)
This project includes `.github/workflows/build.yml`, which builds the
jar automatically in the cloud via GitHub Actions — you don't need Java,
Gradle, or anything else installed on your machine.

1. Create a new repository on GitHub (public or private, doesn't matter)
2. Upload this whole `spear-debug-mod` folder into it (drag-and-drop
   works fine on github.com, or `git push` if you use git)
3. Go to the **Actions** tab of the repo — it will start building
   automatically on push
4. Once it finishes (green checkmark, usually 1-2 minutes), click into
   the run, scroll to **Artifacts**, and download `spear-debug-mod-jar`
5. Unzip that download — inside is the real `.jar` file. That's what
   goes in your `mods` folder.

## Alternative: build locally with IntelliJ IDEA
If you'd rather not use GitHub: install IntelliJ IDEA Community (free),
File → Open → select this folder, let it sync, then run the `build`
Gradle task from the Gradle panel on the right. Jar ends up in
`build/libs/`.

If a build ever fails on version resolution, check the current values
for `loader_version`, `yarn_mappings`, and `fabric_version` in
`gradle.properties` against https://fabricmc.net/develop/ — Fabric's own
versions shift over time.

## How to use it
1. Install **Fabric Loader** for 1.20.1, plus **Fabric API**.
2. Install **ViaFabricPlus** (to actually connect to the 1.21.11 server).
3. Drop this mod's jar into your `mods` folder.
4. Join the server. If you can get your hands on a Spear (crafted,
   looted, or given by another player/admin), hold it and press **K**.
5. Send me:
   - The Item ID (e.g. `minecraft:iron_sword`)
   - The `CustomModelData` value
   - Ideally, do this once per spear material tier (wooden/stone/copper/
     iron/golden/diamond/netherite) if you can get access to more than one

## What happens next
With those values, I'll build:
1. A **CIT Resewn**-based resource pack that reskins the mapped item to
   the real 1.21.11 spear textures/models whenever that `CustomModelData`
   value is present — this fixes the visuals.
2. A **Fabric mixin mod** that replicates the spear's charge/lunge/jab
   attack behavior client-side, based on reverse-engineering how
   ViaBackwards translates the relevant interaction packets. This part is
   more experimental and may need iteration once we can actually test
   against the live server.
