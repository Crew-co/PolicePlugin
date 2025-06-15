package net.crewco.PolicePlugin.guis.listener


// Required dependencies and imports
import net.crewco.PolicePlugin.guis.libs.GuiPage
import net.crewco.PolicePlugin.guis.libs.NoobPage
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


class Gui(private val plugin: Plugin) : Listener {
	private val pages = arrayOfNulls<GuiPage>(100)
	private val viewing = mutableMapOf<UUID, GuiPage>()
	private val li: Listener

	// Quantity of pages
	var size = 0

	init {
		// Event Listener
		this.li = object : Listener {
			@EventHandler
			fun onPluginDisable(event: PluginDisableEvent) {
				for (p in getViewers())
					close(p)
			}

			@EventHandler
			fun onInventoryClick(e: InventoryClickEvent) {
				val p = e.whoClicked as Player
				if (viewing.containsKey(p.uniqueId)) {
					if (viewing[p.uniqueId]!!.i != e.view.topInventory)
						return
					if (viewing[p.uniqueId]!!.shift && e.click.isShiftClick) {
						e.isCancelled = true
						p.updateInventory()
					}
					if (viewing[p.uniqueId]!!.cancel && viewing[p.uniqueId]!!.i == e.clickedInventory) {
						e.isCancelled = true
						p.updateInventory()
					}
					if (viewing[p.uniqueId]!!.getClick() == null)
						return
					viewing[p.uniqueId]!!.getClick()!!.click(e)
					if (e.isCancelled)
						p.updateInventory()
				}
			}

			@EventHandler
			fun onInventoryDrag(e: InventoryDragEvent) {
				val p = e.whoClicked as Player
				if (viewing.containsKey(p.uniqueId)) {
					if (viewing[p.uniqueId]!!.i != e.view.topInventory)
						return
					if (e.inventory.type != viewing[p.uniqueId]!!.i.type)
						return
					if (viewing[p.uniqueId]!!.getClick() != null)
						e.isCancelled = true
					if (viewing[p.uniqueId]!!.getDrag() == null)
						return
					viewing[p.uniqueId]!!.getDrag()!!.drag(e)
				}
			}

			@EventHandler
			fun onClose(e: InventoryCloseEvent) {
				val p = e.player as Player
				if (viewing.containsKey(p.uniqueId)) {
					if (viewing[p.uniqueId]!!.i != e.inventory)
						return
					if (viewing[p.uniqueId]!!.getClose() != null)
						viewing[p.uniqueId]!!.getClose()!!.close(e)
					viewing.remove(p.uniqueId)
				}
			}

			@EventHandler
			fun onInventoryOpen(e: InventoryOpenEvent) {
				val p = e.player as Player
				if (viewing.containsKey(p.uniqueId)) {
					if (viewing[p.uniqueId]!!.i != e.inventory)
						return
					if (viewing[p.uniqueId]!!.getOpen() != null)
						viewing[p.uniqueId]!!.getOpen()!!.open(e)
				}
			}
		}
	}

	// Manually open a gui page to a player
	fun show(p: Player, page: Int): Gui {
		p.closeInventory()
		if (viewing.isEmpty())
			Bukkit.getPluginManager().registerEvents(this.li, plugin)
		this.viewing[p.uniqueId] = pages[page]!!
		p.openInventory(pages[page]!!.i)
		return this
	}

	// Manually close a players inventory
	fun close(p: Player): Gui {
		p.closeInventory()
		this.viewing.remove(p.uniqueId)
		if (viewing.isEmpty())
			HandlerList.unregisterAll(this.li)
		return this
	}

	// List of players viewing the current gui not specific to pages
	fun getViewers(): List<Player> {
		val viewers = mutableListOf<Player>()
		for (u in this.viewing.keys)
			viewers.add(Bukkit.getPlayer(u)!!)
		return viewers
	}

	// Create a page inventory
	fun create(name: String, size: Int): NoobPage {
		val page = NoobPage(color(name)[0], size)
		pages[this.size] = page
		this.size += 1
		return page
	}

	/**
	 * Create many pages based on Itemstack[] size
	 *
	 * @param name   the title for the page
	 * @param size   the page inventory size: 1 = dispenser, 5 = hopper, 9-54 =
	 *               default 9 x size / 9
	 * @param amount the quantity of items from the array per page
	 * @param items  the itemstack array to split for pages
	 * @return First created page
	 */
	fun create(name: String, size: Int, amount: Int, vararg items: ItemStack): NoobPage {
		for (i in 0..items.size / amount) {
			val page = NoobPage(color(name)[0], size, *subArray(items, amount * i, amount * i + amount - 1))
			pages[this.size] = page
			this.size += 1
		}
		return pages[0] as NoobPage
	}

	fun createTemplate(name: String, size: Int): GuiPage {
		return GuiPage(color(name)[0], size)
	}

	fun create(template: GuiPage, amount: Int, vararg items: ItemStack): NoobPage {
		for (i in 0..items.size / amount) {
			val page = template.clone().addItems(*subArray(items, amount * i, amount * i + amount - 1))
			pages[this.size] = NoobPage(page)
			this.size += 1
		}
		return pages[0] as NoobPage
	}

	fun getPage(page: Int): NoobPage {
		return pages[max(0, min(this.size - 1, page))] as NoobPage
	}

	// Opens the next page for a player
	fun nextPage(p: Player): Gui {
		return openPage(p, this.viewing[p.uniqueId]!!.page + 1)
	}

	// Opens the previous page for a player
	fun prevPage(p: Player): Gui {
		return openPage(p, this.viewing[p.uniqueId]!!.page - 1)
	}

	// Manually open a certain page for a player
	private fun openPage(p: Player, page: Int): Gui {
		val to = max(0, min(this.size - 1, page))
		if (this.viewing[p.uniqueId] != pages[to])
			show(p, to)
		return this
	}

	fun getItem(item: ItemStack, name: String?, vararg lore: String): ItemStack {
		val im = item.itemMeta!!
		if (name != null)
			im.setDisplayName(color(name)[0])
		im.lore = color(*lore).toList()
		item.itemMeta = im
		return item
	}

	private fun getItem(matchMaterial: Material, name: String?, vararg lore: String): ItemStack {
		return getItem(ItemStack(matchMaterial), name, *lore)
	}

	@Suppress("DEPRECATION")
	fun getItem(matchMaterial: Material, damage: Short, amount: Int, name: String?, vararg lore: String): ItemStack {
		val item = getItem(matchMaterial, name, *lore)
		item.amount = amount
		item.durability = damage
		return item
	}

	fun color(vararg strings: String): Array<String> {
		val s = Array(strings.size) { "" }
		for (i in strings.indices) {
			if (strings[i] != null)
				s[i] = ChatColor.translateAlternateColorCodes('&', strings[i])
		}
		return s
	}

	fun color(strings: List<String>): Array<String> {
		return color(*strings.toTypedArray())
	}

	fun getInventory(name: String, size: Int): Inventory {
		return when (size) {
			1 -> Bukkit.createInventory(null, InventoryType.DROPPER, name)
			5 -> Bukkit.createInventory(null, InventoryType.HOPPER, name)
			else -> Bukkit.createInventory(null, min(54, max(9, ceil(size / 9.0).toInt() * 9)), name)
		}
	}

	fun interface ClickEvent {
		fun click(e: InventoryClickEvent)
	}

	fun interface CloseEvent {
		fun close(e: InventoryCloseEvent)
	}

	fun interface DragEvent {
		fun drag(e: InventoryDragEvent)
	}

	fun interface OpenEvent {
		fun open(e: InventoryOpenEvent)
	}

	companion object {
		// Macro for getting a subarray
		fun <T> subArray(array: Array<T>, beg: Int, end: Int): Array<T> {
			return array.copyOfRange(beg, end + 1)
		}
	}
}
