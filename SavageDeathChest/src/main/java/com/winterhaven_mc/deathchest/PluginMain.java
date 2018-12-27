package com.winterhaven_mc.deathchest;

import com.winterhaven_mc.deathchest.chests.ChestManager;
import com.winterhaven_mc.deathchest.listeners.BlockEventListener;
import com.winterhaven_mc.deathchest.listeners.InventoryEventListener;
import com.winterhaven_mc.deathchest.listeners.PlayerEventListener;
import com.winterhaven_mc.deathchest.storage.DataStore;
import com.winterhaven_mc.deathchest.storage.DataStoreFactory;
import com.winterhaven_mc.deathchest.commands.CommandManager;
import com.winterhaven_mc.deathchest.messages.MessageManager;
import com.winterhaven_mc.deathchest.util.ProtectionPlugin;

import org.bukkit.plugin.java.JavaPlugin;


/**
 * The main class for SavageDeathChest plugin
 */
public final class PluginMain extends JavaPlugin {

	public static PluginMain instance;

	public WorldManager worldManager;
	public MessageManager messageManager;
	public DataStore dataStore;
	public ChestManager chestManager;

	public boolean debug = getConfig().getBoolean("debug");


	@Override
	public void onEnable() {

		// reference to plugin instance
		instance = this;

		// copy default config from jar if it doesn't exist
		saveDefaultConfig();

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate message manager
		messageManager = new MessageManager(this);

		// instantiate sound configuration

		// instantiate datastore
		dataStore = DataStoreFactory.create();

		// instantiate chest manager
		chestManager = new ChestManager(this);

		// load all chests from datastore
		chestManager.loadDeathChests();

		// instantiate command manager
		new CommandManager(this);

		// initialize event listeners
		new PlayerEventListener(this);
		new BlockEventListener(this);
		new InventoryEventListener(this);

		// log detected protection plugins
		ProtectionPlugin.reportInstalled();
	}


	@Override
	public void onDisable() {

		// close datastore
		dataStore.close();
	}

}
