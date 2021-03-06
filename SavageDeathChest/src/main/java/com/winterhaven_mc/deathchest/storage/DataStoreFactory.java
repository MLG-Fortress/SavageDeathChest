package com.winterhaven_mc.deathchest.storage;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.chests.ChestBlock;
import com.winterhaven_mc.deathchest.chests.DeathChest;

import java.util.*;


/**
 * A factory class for instantiating a data store of the configured type.<br>
 * Note: SQLite is the only available data store type at this time
 */
public final class DataStoreFactory {

	// static reference to main class instance
	private final static PluginMain plugin = PluginMain.instance;


	/**
	 * Create new data store of given type.<br>
	 * No parameter version used when no current datastore exists
	 * and datastore type should be read from configuration
	 * @return new datastore of configured type
	 */
	public static DataStore create() {
		
		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
		if (dataStoreType == null) {
			dataStoreType = DataStoreType.getDefaultType();
		}
		return create(dataStoreType, null);
	}
	
	
	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 * @param dataStoreType		new datastore type
	 * @param oldDataStore		existing datastore reference
	 * @return the new datastore
	 */
	private static DataStore create(final DataStoreType dataStoreType, final DataStore oldDataStore) {
	
		// get new data store of specified type
		DataStore newDataStore = dataStoreType.create();
		
		// initialize new data store
		try {
			newDataStore.initialize();
		} catch (Exception e) {
			plugin.getLogger().severe("Could not initialize " + newDataStore.getName() + " datastore!");
			if (plugin.debug) {
				e.printStackTrace();
			}
		}
		
		// if old data store was passed, convert to new data store
		if (oldDataStore != null) {
			convertDataStore(oldDataStore, newDataStore);
		}
		else {
			convertAll(newDataStore);
		}
		// return initialized data store
		return newDataStore;
	}


	/**
	 * Check if a new datastore type has been configured, and
	 * convert old datastore to new type if necessary
	 */
	public static void reload() {
		
		// get current datastore type
		DataStoreType currentType = plugin.dataStore.getType();
		
		// get configured datastore type
		DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
				
		// if current datastore type does not match configured datastore type, create new datastore
		if (!currentType.equals(newType)) {
			
			// create new datastore
			plugin.dataStore = create(newType,plugin.dataStore);
		}
		
	}


	/**
	 * convert old data store to new data store
	 * @param oldDataStore the existing datastore to be converted from
	 * @param newDataStore the new datastore to be converted to
	 */
	private static void convertDataStore(final DataStore oldDataStore, final DataStore newDataStore) {

		// if datastores are same type, do not convert
		if (oldDataStore.getType().equals(newDataStore.getType())) {
			return;
		}
		
		// if old datastore file exists, attempt to read all records
		if (oldDataStore.exists()) {
			
			plugin.getLogger().info("Converting existing " + oldDataStore.getName() + " datastore to "
					+ newDataStore.getName() + " datastore...");
			
			// initialize old datastore if necessary
			if (!oldDataStore.isInitialized()) {
				try {
					oldDataStore.initialize();
				} catch (Exception e) {
					plugin.getLogger().warning("Could not initialize " 
							+ oldDataStore.getName() + " datastore for conversion.");
					plugin.getLogger().warning(e.getLocalizedMessage());
					return;
				}
			}

			Collection<DeathChest> allChestRecords = oldDataStore.getAllChestRecords();

			Iterator<DeathChest> chestIterator = allChestRecords.iterator();

			int recordCount = 0;

			while (chestIterator.hasNext()) {
				newDataStore.putChestRecord(chestIterator.next());
				recordCount++;
			}

			plugin.getLogger().info(recordCount + " records converted to "
					+ newDataStore.getName() + " datastore.");

			Collection<ChestBlock> allBlockRecords = oldDataStore.getAllBlockRecords();

			Iterator<ChestBlock> blockIterator = allBlockRecords.iterator();

			recordCount = 0;

			while (blockIterator.hasNext()) {
				newDataStore.putBlockRecord(blockIterator.next());
				recordCount++;
			}

			plugin.getLogger().info(recordCount + " records converted to "
					+ newDataStore.getName() + " datastore.");
			
			newDataStore.sync();
			
			oldDataStore.close();
			oldDataStore.delete();
		}
	}

	
	/**
	 * convert all existing data stores to new data store
	 * @param newDataStore the new datastore to convert all other datastores to
	 */
	private static void convertAll(final DataStore newDataStore) {
		
		// get array list of all data store types
		ArrayList<DataStoreType> dataStores = new ArrayList<>(Arrays.asList(DataStoreType.values()));
		
		// remove newDataStore from list of types to convert
		//noinspection SuspiciousMethodCalls
		dataStores.remove(newDataStore);

		for (DataStoreType type : dataStores) {

			// create oldDataStore holder
			DataStore oldDataStore = null;
			
			if (type.equals(DataStoreType.SQLITE)) {
				oldDataStore = new DataStoreSQLite(plugin);
			}
			
			// add additional datastore types here as they become available
			
			if (oldDataStore != null && oldDataStore.exists()) {
				convertDataStore(oldDataStore, newDataStore);
			}
		}
	}
	
}
