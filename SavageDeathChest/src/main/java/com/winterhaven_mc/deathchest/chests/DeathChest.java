package com.winterhaven_mc.deathchest.chests;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.messages.MessageId;
import com.winterhaven_mc.deathchest.sounds.SoundId;
import com.winterhaven_mc.deathchest.tasks.ExpireChestTask;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * A class that represents a death chest, which is comprised of a collection of chest blocks
 */
@Immutable
public final class DeathChest {

	// reference to main class
	private final PluginMain plugin = PluginMain.instance;

	// the UUID of this death chest
	private final UUID chestUUID;

	// the UUID of the owner of this death chest
	private final UUID ownerUUID;

	// the UUID of the player who killed the death chest owner, if any; otherwise null
	private final UUID killerUUID;

	// item count; for future use
	private final int itemCount;

	// placementTime time of this death chest, in milliseconds since epoch
	private final long placementTime;

	// the expirationTime time of this death chest, in milliseconds since epoch
	private final long expirationTime;

	// task id of expire task for this death chest block
	private final int expireTaskId;


	/**
	 * Class constructor
	 * @param chestUUID the chest UUID
	 * @param ownerUUID the chest owner UUID
	 * @param killerUUID the chest killer UUID
	 * @param itemCount the chest item count
	 * @param placementTime the chest placement time
	 * @param expirationTime the chest expiration time
	 */
	public DeathChest(final UUID chestUUID,
					  final UUID ownerUUID,
					  final UUID killerUUID,
					  final int itemCount,
					  final long placementTime,
					  final long expirationTime) {

		this.chestUUID = chestUUID;
		this.ownerUUID = ownerUUID;
		this.killerUUID = killerUUID;
		this.itemCount = itemCount;
		this.placementTime = placementTime;
		this.expirationTime = expirationTime;
		this.expireTaskId = createExpireTask();
	}


	/**
	 * Class constructor
	 * @param player the death chest owner
	 */
	public DeathChest(final Player player) {

		// create random chestUUID
		this.chestUUID = UUID.randomUUID();

		// set playerUUID
		if (player != null && player.getUniqueId() != null) {
			this.ownerUUID = player.getUniqueId();
		}
		else {
			this.ownerUUID = null;
		}

		// set killerUUID
		if (player != null && player.getKiller() != null) {
			this.killerUUID = player.getKiller().getUniqueId();
		}
		else {
			this.killerUUID = null;
		}

		// set item count
		this.itemCount = 0;

		// set placementTime timestamp
		this.placementTime = System.currentTimeMillis();

		// set expirationTime timestamp
		// if configured expiration is zero (or negative), set expiration to zero to signify no expiration
		if (plugin.getConfig().getLong("expire-time") <= 0) {
			this.expirationTime = 0;
		} else {
			// set expiration field based on config setting (converting from minutes to milliseconds)
			this.expirationTime = System.currentTimeMillis()
					+ TimeUnit.MINUTES.toMillis(plugin.getConfig().getLong("expire-time"));
		}

		// set expireTaskId from new expire task
		this.expireTaskId = createExpireTask();
	}


	/**
	 * Getter method for DeathChest chestUUID
	 * @return UUID
	 */
	public final UUID getChestUUID() {
		return chestUUID;
	}


	/**
	 * Getter method for DeathChest ownerUUID
	 * @return UUID
	 */
	public final UUID getOwnerUUID() {
		return ownerUUID;
	}


	/**
	 * Getter method for DeathChest killerUUID
	 * @return UUID
	 */
	public final UUID getKillerUUID() {
		return killerUUID;
	}


	/**
	 * Getter method for DeathChest itemCount
	 * @return integer - itemCount
	 */
	@SuppressWarnings("unused")
	public final int getItemCount() {
		return itemCount;
	}


	/**
	 * Getter method for DeathChest placementTime timestamp
	 * @return long placementTime timestamp
	 */
	public final long getPlacementTime() {
		return this.placementTime;
	}


	/**
	 * Getter method for DeathChest expirationTime timestamp
	 * @return long expirationTime timestamp
	 */
	public final long getExpirationTime() {
		return this.expirationTime;
	}


	/**
	 * Getter method for DeathChest expireTaskId
	 * @return the value of the expireTaskId field in the DeathChest object
	 */
	private int getExpireTaskId() {
		return this.expireTaskId;
	}


	/**
	 * Get chest location. Attempt to get chest location from right chest, left chest or sign in that order.
	 * Returns null if location could not be derived from chest blocks.
	 * @return Location - the chest location or null if no location found
	 */
	public final Location getLocation() {

		Map<ChestBlockType,ChestBlock> chestBlockMap = plugin.chestManager.getChestBlockMap(this.chestUUID);

		if (chestBlockMap.containsKey(ChestBlockType.RIGHT_CHEST)) {
			return chestBlockMap.get(ChestBlockType.RIGHT_CHEST).getLocation();
		}
		else if (chestBlockMap.containsKey(ChestBlockType.LEFT_CHEST)) {
			return chestBlockMap.get(ChestBlockType.LEFT_CHEST).getLocation();
		}
		else if (chestBlockMap.containsKey(ChestBlockType.SIGN)) {
			return chestBlockMap.get(ChestBlockType.SIGN).getLocation();
		}

		return null;
	}


	/**
	 * Set chest metadata on all component blocks
	 */
	final void setMetadata() {

		// set metadata on blocks in set
		for (ChestBlock chestBlock :  plugin.chestManager.getBlockSet(this.chestUUID)) {
			chestBlock.setMetadata(this);
		}
	}


	/**
	 * Test if a player is the owner of this DeathChest
	 * @param player The player to test for DeathChest ownership
	 * @return {@code true} if the player is the DeathChest owner, false if not
     */
	public final boolean isOwner(final Player player) {

		// if ownerUUID is null, return false
		if (this.getOwnerUUID() == null ) {
			return false;
		}
		return this.getOwnerUUID().equals(player.getUniqueId());
	}


	/**
	 * Test if a player is the killer of this DeathChest owner
	 * @param player The player to test for DeathChest killer
	 * @return {@code true} if the player is the killer of the DeathChest owner, false if not
	 */
	public final boolean isKiller(final Player player) {

		// if killer uuid is null, return false
		if (this.getKillerUUID() == null) {
			return false;
		}
		return this.getKillerUUID().equals(player.getUniqueId());
	}


	/**
	 * Transfer all chest contents to player inventory and remove in-game chest if empty.
	 * Items that do not fit in player inventory will retained in chest.
	 * @param player the player whose inventory the chest contents will be transferred
	 */
	public final void autoLoot(final Player player) {

		// if passed player is null, do nothing and return
		if (player == null) {
			return;
		}

		// create ArrayList to hold items that did not fit in player inventory
		Collection<ItemStack> remainingItems = new ArrayList<>();

		// transfer contents of any chest blocks to player, putting any items that did not fit in remainingItems
		for (ChestBlock chestBlock : plugin.chestManager.getBlockSet(this.chestUUID)) {
			remainingItems.addAll(chestBlock.transferContents(player));
		}

		// if remainingItems is empty, all chest items fit in player inventory so destroy chest and return
		if (remainingItems.isEmpty()) {
			this.destroy();
			return;
		}

		// send player message
		plugin.messageManager.sendMessage(player, MessageId.INVENTORY_FULL, this);

		// try to put remaining items back in chest
		remainingItems = this.fill(remainingItems);

		// if remainingItems is still not empty, items could not be placed back in chest, so drop items at player location
		// this should never actually occur, but let's play it safe just in case
		if (!remainingItems.isEmpty()) {
			for (ItemStack itemStack : remainingItems) {
				player.getWorld().dropItem(player.getLocation(),itemStack);
			}
		}
	}


	/**
	 * Expire this death chest
	 */
	public final void expire() {

		// get player from ownerUUID
		final Player player = plugin.getServer().getPlayer(this.ownerUUID);

		// destroy DeathChest
		this.destroy();

		// if player is not null, send player message
		if (player != null) {
			plugin.messageManager.sendMessage(player, MessageId.CHEST_EXPIRED, this);
		}
	}


	/**
	 * Destroy this death chest, dropping chest contents
	 */
	public final void destroy() {

		// play chest break sound at chest location

		// get block map for this chest
		Map<ChestBlockType,ChestBlock> chestBlockMap = plugin.chestManager.getChestBlockMap(this.chestUUID);

		// destroy DeathChest blocks (sign gets destroyed first due to enum order)
		for (ChestBlock chestBlock : chestBlockMap.values()) {
			chestBlock.destroy();
		}

		// delete DeathChest record from datastore
		plugin.dataStore.deleteChestRecord(this);

		// cancel expire block task
		if (this.getExpireTaskId() > 0) {
			plugin.getServer().getScheduler().cancelTask(this.getExpireTaskId());
		}

		// remove DeathChest from ChestManager DeathChest map
		plugin.chestManager.removeDeathChest(this);
	}


	/**
	 * Get inventory associated with this death chest
	 * @return Inventory - the inventory associated with this death chest;
	 * returns null if both right and left chest block inventories are invalid
	 */
	public final Inventory getInventory() {

		// get chest block map
		Map<ChestBlockType,ChestBlock> chestBlocks = plugin.chestManager.getChestBlockMap(this.chestUUID);

		// get right chest inventory
		Inventory inventory = chestBlocks.get(ChestBlockType.RIGHT_CHEST).getInventory();

		// if right chest inventory is null, try left chest
		if (inventory == null) {
			inventory = chestBlocks.get(ChestBlockType.LEFT_CHEST).getInventory();
		}

		// return the inventory, or null if right and left chest inventories were both invalid
		return inventory;
	}


	/**
	 * Get the number of players currently viewing a DeathChest inventory
	 * @return The number of inventory viewers
     */
	public final int getViewerCount() {

		// get chest inventory
		Inventory inventory = this.getInventory();

		// if inventory is not null, return viewer count
		if (inventory != null) {
			return inventory.getViewers().size();
		}
		else {
			// inventory is null, so return 0 for viewer count
			return 0;
		}
	}


	/**
	 * Create expire chest task
	 */
	private int createExpireTask() {

		// if DeathChestBlock expirationTime is zero or less, it is set to never expire
		if (this.getExpirationTime() < 1) {
			return -1;
		}

		// get current time
		Long currentTime = System.currentTimeMillis();

		// compute ticks remaining until expire time (millisecond interval divided by 50 yields ticks)
		long ticksRemaining = (this.expirationTime - currentTime) / 50;
		if (ticksRemaining < 1) {
			ticksRemaining = 1L;
		}

		// create task to expire death chest after ticksRemaining
		BukkitTask chestExpireTask = new ExpireChestTask(this).runTaskLater(plugin, ticksRemaining);

		// return taskId
		return chestExpireTask.getTaskId();
	}


	/**
	 * Cancel expire task for this death chest
	 */
	void cancelExpireTask() {

		// if task id is positive integer, cancel task
		if (this.expireTaskId > 0) {
			plugin.getServer().getScheduler().cancelTask(this.expireTaskId);
		}
	}


	/**
	 * Place collection of ItemStacks in chest, returning collection of ItemStacks that did not fit in chest
	 * @param itemStacks Collection of ItemStacks to place in chest
	 * @return Collection of ItemStacks that did not fit in chest
	 */
	final Collection<ItemStack> fill(final Collection<ItemStack> itemStacks) {

		// create empty list for return
		Collection<ItemStack> remainingItems = new ArrayList<>();

		// get inventory for this death chest
		Inventory inventory = this.getInventory();

		// if inventory is not null, add itemStacks to inventory and put leftovers in remainingItems
		if (inventory != null) {
			remainingItems = new ArrayList<>(inventory.addItem(itemStacks.toArray(new ItemStack[0])).values());
		}

		// return collection of items that did not fit in inventory
		return remainingItems;
	}

}
