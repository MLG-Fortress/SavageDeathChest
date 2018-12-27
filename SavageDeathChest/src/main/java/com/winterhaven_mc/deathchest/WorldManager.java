package com.winterhaven_mc.deathchest;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@SuppressWarnings("unused")
public final class WorldManager {

    // reference to main class
    private final Plugin plugin;

    // list of enabled world names
    private final List<UUID> enabledWorldUIDs = new ArrayList<>();


    /**
     * Class constructor
     * @param plugin passed reference to the plugin main class
     */
    public WorldManager(final JavaPlugin plugin) {

        // set reference to main class
        this.plugin = plugin;

        // populate enabled world UID list field
        this.reload();
    }


    /**
     * update enabledWorlds ArrayList field from plugin config.yml file
     */
    @SuppressWarnings("WeakerAccess")
    public final void reload() {

        // clear enabledWorldUIDs field
        this.enabledWorldUIDs.clear();

        // if config list of enabled worlds is empty, add all server worlds
        if (plugin.getConfig().getStringList("enabled-worlds").isEmpty()) {

            // iterate through all server worlds
            for (World world : plugin.getServer().getWorlds()) {

                // add world UID to field if it is not already in list
                if (!this.enabledWorldUIDs.contains(world.getUID())) {
                    this.enabledWorldUIDs.add(world.getUID());
                }
            }
        }
        // otherwise, add only the worlds in the config enabled worlds list
        else {
            // iterate through config list of enabled worlds, and add valid world UIDs to field
            for (String worldName : plugin.getConfig().getStringList("enabled-worlds")) {

                // get world by name
                World world = plugin.getServer().getWorld(worldName);

                // add world UID to field if it is not already in list and world exists
                if (world != null && !this.enabledWorldUIDs.contains(world.getUID())) {
                    this.enabledWorldUIDs.add(world.getUID());
                }
            }
        }

        // remove config list of disabled worlds from enabledWorldUIDs field
        for (String worldName : plugin.getConfig().getStringList("disabled-worlds")) {

            // get world by name
            World world = plugin.getServer().getWorld(worldName);

            // if world is not null remove UID from list
            if (world != null) {
                this.enabledWorldUIDs.remove(world.getUID());
            }
        }
    }


    /**
     * get list of enabled world names from plugin config.yml file
     * @return an ArrayList of String containing enabled world names
     */
    public final List<String> getEnabledWorldNames() {

        // create empty list of string for return
        List<String> resultList = new ArrayList<>();

        // iterate through list of enabled world UIDs
        for (UUID worldUID : this.enabledWorldUIDs) {

            // get world by UID
            World world = plugin.getServer().getWorld(worldUID);

            // if world is not null, add name to return list
            if (world != null) {
                resultList.add(world.getName());
            }
        }

        // return result list
        return resultList;
    }


    /**
     * Check if a world is enabled by bukkit world UID
     * @param worldUID Unique Identifier for world
     * @return true if enabled, false if disabled
     */
    public final boolean isEnabled(final UUID worldUID) {

        // if worldUID is null return false
        if (worldUID == null) {
            return false;
        }

        return this.enabledWorldUIDs.contains(worldUID);
    }


    /**
     * Check if a world is enabled by bukkit world object
     * @param world bukkit world object
     * @return true if enabled, false if disabled
     */
    public final boolean isEnabled(final World world) {

        // if world is null return false
        if (world == null) {
            return false;
        }

        return this.enabledWorldUIDs.contains(world.getUID());
    }


    /**
     * Check if a world is enabled by name
     * @param worldName name of world as string to check
     * @return {@code true} if world is enabled, {@code false} if not
     */
    public final boolean isEnabled(final String worldName) {

        // if worldName is null or empty, return false
        if (worldName == null || worldName.isEmpty()) {
            return false;
        }

        // get world by name
        World world = plugin.getServer().getWorld(worldName);

        // if world is null, return false
        if (world == null) {
            return false;
        }

        return (this.enabledWorldUIDs.contains(world.getUID()));
    }


    /**
     * Get world name from world UID. If a Multiverse alias exists for the world, it will be returned;
     * otherwise the bukkit world name will be returned
     * @param worldUID the unique ID of a bukkit world
     * @return String containing Multiverse world alias or bukkit world name
     */
    public final String getWorldName(final UUID worldUID) {

        // if worldUID is null, return null
        if (worldUID == null) {
            return null;
        }

        // get world
        World world = plugin.getServer().getWorld(worldUID);

        // if world is null, return null
        if (world == null) {
            return null;
        }

        // get bukkit world name
        String worldName = world.getName();

        // return the bukkit world name or Multiverse world alias
        return worldName;
    }

    /**
     * Get world name from world object, using Multiverse alias if available
     * @param world the world object to retrieve name
     * @return world name or multiverse alias as String
     */
    public final String getWorldName(final World world) {

        // if world is null, return null
        if (world == null) {
            return null;
        }

        // get bukkit world name
        String worldName = world.getName();

        // return the bukkit world name or Multiverse world alias
        return worldName;
    }


    /**
     * Get world name from world name string, using Multiverse alias if available
     * @param passedName the bukkit world name as string
     * @return bukkit world name or multiverse alias as String
     */
    public final String getWorldName(final String passedName) {

        // if passedName is null or empty, return null
        if (passedName == null || passedName.isEmpty()) {
            return null;
        }

        // get world
        World world = plugin.getServer().getWorld(passedName);

        // if world is null, return null
        if (world == null) {
            return null;
        }

        // get bukkit world name
        String worldName = world.getName();

        // return the bukkit world name or Multiverse world alias
        return worldName;
    }


    /**
     * Get world name for command sender's world, using Multiverse alias if available
     * @param sender the command sender used to retrieve world name
     * @return bukkit world name or multiverse alias as String
     */
    public final String getWorldName(final CommandSender sender) {

        // if passedName is null, return null
        if (sender == null) {
            return null;
        }

        World world = plugin.getServer().getWorlds().get(0);

        if (sender instanceof Entity) {
            world = ((Entity) sender).getWorld();
        }

        String worldName = world.getName();

        // return the bukkit world name or Multiverse world alias
        return worldName;
    }


    /**
     * get world spawn location, preferring Multiverse spawn location if available
     * @param world bukkit world object to retrieve spawn location
     * @return spawn location
     */
    public final Location getSpawnLocation(final World world) {

        // return bukkit world spawn location
        return world.getSpawnLocation();
    }


    /**
     * get world spawn location for entity, preferring Multiverse spawn location if available
     * @param entity entity to retrieve world spawn location
     * @return world spawn location
     */
    public final Location getSpawnLocation(final Entity entity) {

        // return bukkit world spawn location
        return entity.getWorld().getSpawnLocation();
    }

}