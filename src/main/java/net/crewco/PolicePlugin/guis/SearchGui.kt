package net.crewco.PolicePlugin.guis

import net.crewco.PolicePlugin.Startup
import net.crewco.PolicePlugin.Startup.Companion.utilsManager
import net.crewco.PolicePlugin.guis.libs.NoobPage
import net.crewco.PolicePlugin.guis.listener.Gui
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.inventory.ItemStack
import java.util.*
import javax.inject.Inject

class SearchGui @Inject constructor(private val plugin:Startup) {
	fun create(selector: Player, target: Player): Gui {
		val lore: MutableList<String> = ArrayList()
		lore.add(utilsManager.color("&7&lOfficer: &f" + selector.name))
		lore.add(utilsManager.color("&7&lDate: &f" + (Date()).toString()))
		val taken: MutableList<String> = ArrayList()
		taken.add(utilsManager.color("&7&lTaken:"))
		val added: MutableList<String> = ArrayList()
		added.add(utilsManager.color("&7&lAdded:"))
		val gui =Gui(plugin)
		val page: NoobPage = gui.create(target.name, 36)
		val items = listOf(*target.inventory.storageContents)
		page.setContents(target.inventory.storageContents)
		val take = listOf(
			InventoryAction.COLLECT_TO_CURSOR, InventoryAction.PICKUP_ALL,
			InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_SOME
		)
		val add = listOf(
			InventoryAction.DROP_ALL_CURSOR, InventoryAction.DROP_ALL_SLOT,
			InventoryAction.DROP_ONE_CURSOR, InventoryAction.DROP_ONE_SLOT, InventoryAction.PLACE_ALL,
			InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME
		)
		page.onClick { e ->
			if (e!!.action != InventoryAction.MOVE_TO_OTHER_INVENTORY
				&& (!add.contains(e.action) && !take.contains(e.action))
			) {
				e.isCancelled = true
			} else if (e.currentItem != null && e.currentItem!!.hasItemMeta()
				&& e.currentItem!!.itemMeta.hasDisplayName()
				&& (e.currentItem!!.itemMeta.displayName.startsWith(utilsManager.color("&bSearch Report")) || e
					.currentItem!!.itemMeta.displayName.startsWith(utilsManager.color("&bJail File")))
			) {
				e.isCancelled = true
			} else if (take.contains(e.action) && !e.whoClicked.hasPermission("prison.search.edit")) {
				e.isCancelled = true
			} else if (add.contains(e.action) && !e.whoClicked.hasPermission("prison.search.edit")) {
				e.isCancelled = true
			} else if (e.action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
				if (!e.whoClicked.hasPermission("prison.search.edit")
					&& e.clickedInventory!! == e.view.topInventory
				) e.isCancelled = true
				else if (!e.whoClicked.hasPermission("prison.search.edit")
					&& e.clickedInventory!! == e.view.bottomInventory
				) e.isCancelled = true
			}
			Bukkit.getScheduler().runTaskLater(plugin, Runnable {
				target.inventory.storageContents = e.inventory.storageContents
				target.updateInventory()
			}, 2)
		}
		page.onClose { e ->
			for (i in items.indices) {
				if (e != null) {
					if (items[i] != null && !e.inventory.contains(items[i])) taken.add(
						"&b- &c" + (if (items[i]!!.hasItemMeta() && items[i]!!
								.itemMeta.hasDisplayName()
						)
							ChatColor.stripColor(items[i]!!.itemMeta.displayName)
						else
							items[i]!!.type.toString()) + "x" + items[i]!!.amount
					)
					else if (e.inventory.storageContents[i] != null
						&& !items.contains(e.inventory.storageContents[i])
					) added.add(
						("&b- &c"
								+ (if (e.inventory.storageContents[i]!!.hasItemMeta()
							&& e.inventory.storageContents[i]!!.itemMeta.hasDisplayName()
						)
							ChatColor.stripColor(
								e.inventory.storageContents[i]!!
									.itemMeta.getDisplayName()
							)
						else
							e.inventory.storageContents[i]!!.type.toString())
								+ "x" + e.inventory.storageContents[i]!!.amount)
					)
				}
			}
			if (e != null) {
				target.inventory.storageContents = e.inventory.storageContents
			}
			if (taken.size > 1) {
				lore.addAll(taken)
			}
			if (added.size > 1) {
				lore.addAll(added)
			}
			if (lore.size in 3..15) {
				val slip: ItemStack = utilsManager.getItem(ItemStack(Material.PAPER), "&bSearch Report", lore)
				target.inventory.addItem(slip)
				selector.inventory.addItem(slip)
			} else if (lore.size in 16..30) {
				val slip: ItemStack = utilsManager.getItem(ItemStack(Material.PAPER), "&bSearch Report #1", lore.subList(0, 15))
				target.inventory.addItem(slip)
				selector.inventory.addItem(slip)
				val slip2: ItemStack = utilsManager.getItem(
					ItemStack(Material.PAPER), "&bSearch Report #2",
					lore.subList(15, lore.size)
				)
				target.inventory.addItem(slip2)
				selector.inventory.addItem(slip2)
			} else if (lore.size > 30) {
				val slip: ItemStack = utilsManager.getItem(ItemStack(Material.PAPER), "&bSearch Report #1", lore.subList(0, 15))
				target.inventory.addItem(slip)
				selector.inventory.addItem(slip)
				val slip2: ItemStack = utilsManager.getItem(
					ItemStack(Material.PAPER), "&bSearch Report #2",
					lore.subList(15, 30)
				)
				target.inventory.addItem(slip2)
				selector.inventory.addItem(slip2)
				val slip3: ItemStack = utilsManager.getItem(
					ItemStack(Material.PAPER), "&bSearch Report #3",
					lore.subList(30, lore.size)
				)
				target.inventory.addItem(slip3)
				selector.inventory.addItem(slip3)
			}
		}
		return gui
	}
}