package net.crewco.PolicePlugin.guis.listener

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
	private val viewing: MutableMap<UUID, GuiPage?> = HashMap()
	private val li: Listener

	// Quantity of pages
	var size: Int = 0

	init {
		// Event Listener
		this.li = object : Listener {
			@EventHandler
			fun onPluginDisable(event: PluginDisableEvent?) {
				for (p in viewers) p?.let { close(it) }
			}

			@EventHandler
			fun onInventoryClick(e: InventoryClickEvent) {
				val p = e.whoClicked as Player
				if (viewing.containsKey(p.uniqueId)) {
					if (viewing[p.uniqueId]!!.i != e.view.topInventory) return
					if (viewing[p.uniqueId]!!.shift && e.click.isShiftClick) {
						e.isCancelled = true
						p.updateInventory()
					}
					if (viewing[p.uniqueId]!!.cancel
						&& viewing[p.uniqueId]!!.i == e.clickedInventory
					) {
						e.isCancelled = true
						p.updateInventory()
					}
					if (viewing[p.uniqueId]!!.click == null) return
					viewing[p.uniqueId]!!.click!!.click(e)
					if (e.isCancelled) p.updateInventory()
				}
			}

			@EventHandler
			fun onInventoryDrag(e: InventoryDragEvent) {
				val p = e.whoClicked as Player
				if (viewing.containsKey(p.uniqueId)) {
					if (viewing[p.uniqueId]!!.i != e.view.topInventory) return
					if (e.inventory.type != viewing[p.uniqueId]!!.i.type) return
					if (viewing[p.uniqueId]!!.click != null) e.isCancelled = true
					if (viewing[p.uniqueId]!!.drag == null) return
					viewing[p.uniqueId]!!.drag!!.drag(e)
				}
			}

			@EventHandler
			fun onClose(e: InventoryCloseEvent) {
				val p = e.player as Player
				if (viewing.containsKey(p.uniqueId)) {
					if (viewing[p.uniqueId]!!.i != e.inventory) return
					if (viewing[p.uniqueId]!!.close != null) viewing[p.uniqueId]!!.close!!.close(e)
					viewing.remove(p.uniqueId)
				}
			}

			@EventHandler
			fun onInventoryOpen(e: InventoryOpenEvent) {
				val p = e.player as Player
				if (viewing.containsKey(p.uniqueId)) {
					if (viewing[p.uniqueId]!!.i != e.inventory) return
					if (viewing[p.uniqueId]!!.open != null) viewing[p.uniqueId]!!.open!!.open(e)
				}
			}
		}
	}

	// Manually open a gui page to a player
	fun show(p: Player, page: Int): Gui {
		p.closeInventory()
		if (viewing.isEmpty()) Bukkit.getPluginManager().registerEvents(this.li, plugin)
		viewing[p.uniqueId] = pages[page]
		p.openInventory(pages[page]!!.i)
		return this
	}

	// Manually close a players inventory
	fun close(p: Player): Gui {
		p.closeInventory()
		viewing.remove(p.uniqueId)
		if (viewing.isEmpty()) HandlerList.unregisterAll(this.li)
		return this
	}

	val viewers: List<Player?>
		// List of players viewing the current gui not specific to pages
		get() {
			val viewers: MutableList<Player?> = ArrayList()
			for (u in viewing.keys) viewers.add(Bukkit.getPlayer(u))
			return viewers
		}

	// Create a page inventory
	fun create(name: String, size: Int): NoobPage {
		val page = NoobPage(color(name)[0]!!, size)
		pages[this.size] = page
		this.size += 1
		return page
	}

	/**
	 * Create many pages based on Itemstack[] size
	 *
	 * @param name   the title for the page
	 * @param size   the page inventory size: 1 = dispenser, 5 = hopper, 9-54 =
	 * default 9 x size / 9
	 * @param amount the quantity of items from the array per page
	 * @param items  the itemstack array to split for pages
	 * @return First created page
	 */
	fun create(name: String, size: Int, amount: Int, vararg items: ItemStack?): NoobPage? {
		// Remove nulls first
		val nonNullItems = items.filterNotNull().toTypedArray()

		for (i in 0..nonNullItems.size / amount) {
			val page = NoobPage(
				color(name)[0]!!,
				size,
				*subArray<ItemStack>(nonNullItems, amount * i, amount * i + amount - 1)
			)
			pages[this.size] = page
			this.size += 1
		}

		return pages[0] as NoobPage?
	}

	fun createTemplate(name: String, size: Int): GuiPage {
		return GuiPage(color(name)[0]!!, size)
	}

	fun create(template: GuiPage, amount: Int, vararg items: ItemStack?): NoobPage? {
		val nonNullItems = items.filterNotNull().toTypedArray()

		for (i in 0..nonNullItems.size / amount) {
			val chunk = subArray(nonNullItems, amount * i, amount * i + amount - 1)
			val page = template.clone().addItems(*chunk)
			pages[size] = NoobPage(page)
			this.size += 1
		}

		return pages[0] as NoobPage?
	}

	fun getPage(page: Int): NoobPage? {
		return pages[max(0.0, min((this.size - 1).toDouble(), page.toDouble())).toInt()] as NoobPage?
	}

	// Opens the next page for a player
	fun nextPage(p: Player): Gui {
		return openPage(p, viewing[p.uniqueId]!!.page + 1)
	}

	// Opens the previous page for a player
	fun prevPage(p: Player): Gui {
		return openPage(p, viewing[p.uniqueId]!!.page - 1)
	}

	// Manually open a certain page for a player
	fun openPage(p: Player, page: Int): Gui {
		val to = max(0.0, min((this.size - 1).toDouble(), page.toDouble())).toInt()
		if (viewing[p.uniqueId] != pages[to]) show(p, to)
		return this
	}

	// Christos Naming Version of GUIPage
	inner class NoobPage : GuiPage {
		constructor(name: String, size: Int, vararg items: ItemStack?) : super(name, size, *items)

		constructor(template: GuiPage) : super(template)

		// Disable the ability to click items in the page
		fun c(): NoobPage {
			super.noClick()
			return this
		}

		// Disable the ability to shift items into the page
		fun s(): NoobPage {
			super.noShift()
			return this
		}

		fun cl(): NoobPage {
			super.clear()
			return this
		}

		fun clR(row: Int): NoobPage {
			super.clearRow(row)
			return this
		}

		fun a(item: ItemStack?): NoobPage {
			super.addItem(item)
			return this
		}

		fun a(item: Material): NoobPage {
			super.addItem(item)
			return this
		}

		fun a(item: ItemStack, name: String?, vararg lore: String?): NoobPage {
			super.addItem(getItem(item, name, *lore))
			return this
		}

		fun a(item: Material, name: String?, vararg lore: String?): NoobPage {
			super.addItem(getItem(ItemStack(item), name, *lore))
			return this
		}

		fun a(vararg items: ItemStack?): NoobPage {
			super.addItems(*items)
			return this
		}

		fun a(vararg items: Material?): NoobPage {
			super.addItems(*items)
			return this
		}

		fun i(position: Int, item: ItemStack?): NoobPage {
			super.setItem(position, item)
			return this
		}

		fun i(position: Int, item: Material): NoobPage {
			super.setItem(position, item)
			return this
		}

		fun i(position: Int, item: ItemStack, name: String?, vararg lore: String?): NoobPage {
			super.setItem(position, getItem(item, name, *lore))
			return this
		}

		fun i(position: Int, item: Material, name: String?, vararg lore: String?): NoobPage {
			super.setItem(position, getItem(ItemStack(item), name, *lore))
			return this
		}

		fun i(row: Int, column: Int, item: ItemStack?): NoobPage {
			super.setItem(column + row * 9, item)
			return this
		}

		fun i(row: Int, column: Int, item: Material): NoobPage {
			super.setItem(column + row * 9, item)
			return this
		}

		fun i(row: Int, column: Int, item: ItemStack, name: String?, vararg lore: String?): NoobPage {
			super.setItem(column + row * 9, getItem(item, name, *lore))
			return this
		}

		fun i(row: Int, column: Int, item: Material, name: String?, vararg lore: String?): NoobPage {
			super.setItem(column + row * 9, getItem(ItemStack(item), name, *lore))
			return this
		}

		fun i(item: ItemStack?, vararg slots: Int): NoobPage {
			super.setItems(item, *slots)
			return this
		}

		fun i(item: Material, vararg slots: Int): NoobPage {
			super.setItems(item, *slots)
			return this
		}

		fun i(slots: List<Int>, item: ItemStack?): NoobPage {
			super.setItems(slots, item)
			return this
		}

		fun i(slots: List<Int>, item: Material): NoobPage {
			super.setItems(slots, item)
			return this
		}

		fun i(slots: List<Int>, item: ItemStack, name: String?, vararg lore: String?): NoobPage {
			super.setItems(slots, item, name, *lore)
			return this
		}

		fun i(slots: List<Int>, item: Material, name: String?, vararg lore: String?): NoobPage {
			super.setItems(slots, item, name, *lore)
			return this
		}

		fun fc(column: Int, item: ItemStack?): NoobPage {
			super.fillColumn(column, item)
			return this
		}

		fun fc(column: Int, item: Material): NoobPage {
			super.fillColumn(column, ItemStack(item))
			return this
		}

		fun fc(column: Int, item: ItemStack, name: String?, vararg lore: String?): NoobPage {
			super.fillColumn(column, getItem(item, name, *lore))
			return this
		}

		fun fc(column: Int, item: Material, name: String?, vararg lore: String?): NoobPage {
			super.fillColumn(column, getItem(ItemStack(item), name, *lore))
			return this
		}

		fun fr(row: Int, item: ItemStack?): NoobPage {
			super.fillRow(row, item)
			return this
		}

		fun fr(row: Int, item: Material): NoobPage {
			super.fillRow(row, item)
			return this
		}

		fun fr(row: Int, item: ItemStack, name: String?, vararg lore: String?): NoobPage {
			super.fillRow(row, item, name, *lore)
			return this
		}

		fun fr(row: Int, item: Material, name: String?, vararg lore: String?): NoobPage {
			super.fillRow(row, item, name, *lore)
			return this
		}

		fun f(item: ItemStack?): NoobPage {
			super.fill(item)
			return this
		}

		fun f(item: Material): NoobPage {
			super.fill(item)
			return this
		}

		fun f(item: ItemStack, name: String?, vararg lore: String?): NoobPage {
			super.fill(getItem(item, name, *lore))
			return this
		}

		fun f(item: Material, name: String?, vararg lore: String?): NoobPage {
			super.fill(getItem(ItemStack(item), name, *lore))
			return this
		}

		override fun onClick(event: clickEvent?): NoobPage {
			super.onClick(event)
			return this
		}

		override fun onClose(event: closeEvent?): NoobPage {
			super.onClose(event)
			return this
		}

		override fun onOpen(event: openEvent?): NoobPage {
			super.onOpen(event)
			return this
		}

		override fun onDrag(event: dragEvent?): NoobPage {
			super.onDrag(event)
			return this
		}
	}

	open inner class GuiPage {
		val size: Int
		val page: Int
		val name: String
		val i: Inventory
		var click: clickEvent? = null
			private set
		var open: openEvent? = null
			private set
		var close: closeEvent? = null
			private set
		var drag: dragEvent? = null
			private set
		var cancel: Boolean = false
		var shift: Boolean = false

		constructor(name: String, size: Int, vararg items: ItemStack?) {
			this.name = name
			this.i = getInventory(name, size)
			this.size = i.size
			i.contents = items
			this.page = this@Gui.size
		}

		constructor(template: GuiPage) {
			this.name = template.name
			this.i = getInventory(template.name, template.size)
			this.size = i.size
			i.contents = template.contents
			this.page = this@Gui.size
			this.cancel = template.cancel
			this.shift = template.shift
			this.onClick(template.click)
			this.onDrag(template.drag)
			this.onClose(template.close)
			this.onOpen(template.open)
		}

		val contents: Array<ItemStack?>
			get() = i.contents

		fun setContents(contents: Array<ItemStack?>): GuiPage {
			i.contents = contents
			return this
		}

		// Disable the ability to click items in the page
		fun noClick(): GuiPage {
			this.cancel = true
			return this
		}

		fun clone(): GuiPage {
			return GuiPage(this)
		}

		// Disable the ability to shift items into the page
		fun noShift(): GuiPage {
			this.shift = true
			return this
		}

		fun addItem(item: ItemStack?): GuiPage {
			if (i.firstEmpty() == -1) {
				println("Error adding item: Inventory was not empty.")
				return this
			}
			setItem(i.firstEmpty(), item)
			return this
		}

		fun clear(): GuiPage {
			i.contents = arrayOfNulls<ItemStack>(i.contents.size)
			return this
		}

		fun addItem(item: Material): GuiPage {
			setItem(i.firstEmpty(), ItemStack(item))
			return this
		}

		fun addGlow(position: Int): GuiPage {
			val item = i.getItem(position)
			val meta = item!!.itemMeta
			meta.addEnchant(Enchantment.LURE, 1, true)
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
			item.setItemMeta(meta)
			i.setItem(position, item)
			return this
		}

		fun addGlow(row: Int, column: Int): GuiPage {
			val position = column + row * 9
			val item = i.getItem(position)
			val meta = item!!.itemMeta
			meta.addEnchant(Enchantment.LURE, 1, true)
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
			item.setItemMeta(meta)
			i.setItem(position, item)
			return this
		}

		fun removeGlow(position: Int): GuiPage {
			val item = i.getItem(position)
			val meta = item!!.itemMeta
			meta.removeEnchant(Enchantment.LURE)
			item.setItemMeta(meta)
			i.setItem(position, item)
			return this
		}

		fun removeGlow(row: Int, column: Int): GuiPage {
			val position = column + row * 9
			val item = i.getItem(position)
			val meta = item!!.itemMeta
			meta.removeEnchant(Enchantment.LURE)
			item.setItemMeta(meta)
			i.setItem(position, item)
			return this
		}

		fun addItem(item: ItemStack, name: String?, vararg lore: String?): GuiPage {
			return addItem(getItem(item, name, *lore))
		}

		fun addItem(item: Material, name: String?, vararg lore: String?): GuiPage {
			return addItem(getItem(ItemStack(item), name, *lore))
		}

		fun setItem(position: Int, item: ItemStack?): GuiPage {
			i.setItem(position, item)
			return this
		}

		fun setItem(position: Int, item: Material): GuiPage {
			return setItem(position, ItemStack(item))
		}

		fun setItem(position: Int, item: ItemStack, name: String?, vararg lore: String?): GuiPage {
			return setItem(position, getItem(item, name, *lore))
		}

		fun setItem(position: Int, item: Material, name: String?, vararg lore: String?): GuiPage {
			return setItem(position, getItem(ItemStack(item), name, *lore))
		}

		fun setItem(row: Int, column: Int, item: ItemStack?): GuiPage {
			return setItem(column + row * 9, item)
		}

		fun setItem(row: Int, column: Int, item: Material): GuiPage {
			return setItem(column + row * 9, item)
		}

		fun setItem(row: Int, column: Int, item: ItemStack, name: String?, vararg lore: String?): GuiPage {
			return setItem(column + row * 9, getItem(item, name, *lore))
		}

		fun setItem(row: Int, column: Int, item: Material, name: String?, vararg lore: String?): GuiPage {
			return setItem(column + row * 9, getItem(ItemStack(item), name, *lore))
		}

		fun fillColumn(column: Int, item: ItemStack?): GuiPage {
			for (i in 0..8) {
				if (i * 9 + column >= this.i.size) break
				if (this.i.contents[i * 9 + column] == null) setItem(i * 9 + column, item)
			}
			return this
		}

		fun fillColumn(column: Int, item: Material): GuiPage {
			return fillColumn(column, ItemStack(item))
		}

		fun fillColumn(column: Int, item: ItemStack, name: String?, vararg lore: String?): GuiPage {
			return fillColumn(column, getItem(item, name, *lore))
		}

		fun fillColumn(column: Int, item: Material, name: String?, vararg lore: String?): GuiPage {
			return fillColumn(column, getItem(ItemStack(item), name, *lore))
		}

		fun fillRow(row: Int, item: ItemStack?): GuiPage {
			for (i in 0..8) {
				if (row * 9 + i >= this.i.size) break
				if (this.i.contents[row * 9 + i] == null) setItem(row * 9 + i, item)
			}
			return this
		}

		fun fillRow(row: Int, item: Material): GuiPage {
			return fillRow(row, ItemStack(item))
		}

		fun fillRow(row: Int, item: ItemStack, name: String?, vararg lore: String?): GuiPage {
			return fillRow(row, getItem(item, name, *lore))
		}

		fun fillRow(row: Int, item: Material, name: String?, vararg lore: String?): GuiPage {
			return fillRow(row, getItem(ItemStack(item), name, *lore))
		}

		fun addItems(vararg items: ItemStack?): GuiPage {
			for (item in items) if (item != null) addItem(item)
			return this
		}

		fun addItems(vararg items: Material?): GuiPage {
			for (item in items) if (item != null) addItem(item)
			return this
		}

		fun fill(item: ItemStack?): GuiPage {
			for (i in 0..<i.size) if (this.i.contents[i] == null) setItem(i, item)
			return this
		}

		fun fill(item: Material): GuiPage {
			return fill(ItemStack(item))
		}

		fun fill(item: ItemStack, name: String?, vararg lore: String?): GuiPage {
			return fill(getItem(item, name, *lore))
		}

		fun fill(item: Material, name: String?, vararg lore: String?): GuiPage {
			return fill(getItem(ItemStack(item), name, *lore))
		}

		fun setItems(item: ItemStack?, vararg slots: Int): GuiPage {
			for (i in slots.indices) this.i.setItem(slots[i], item)
			return this
		}

		fun setItems(item: Material, vararg slots: Int): GuiPage {
			return setItems(ItemStack(item), *slots)
		}

		fun setItems(slots: List<Int>, item: ItemStack?): GuiPage {
			for (slot in slots) setItem(slot, item)
			return this
		}

		fun setItems(slots: List<Int>, item: Material): GuiPage {
			return setItems(slots, ItemStack(item))
		}

		fun setItems(slots: List<Int>, item: ItemStack, name: String?, vararg lore: String?): GuiPage {
			for (slot in slots) setItem(slot, item, name, *lore)
			return this
		}

		fun setItems(slots: List<Int>, item: Material, name: String?, vararg lore: String?): GuiPage {
			return setItems(slots, getItem(ItemStack(item), name, *lore))
		}

		fun clearRow(row: Int): GuiPage {
			for (i in 0..8) {
				if (row * 9 + i >= this.i.size) break
				if (this.i.contents[row * 9 + i] != null) setItem(row * 9 + i, ItemStack(Material.AIR))
			}
			return this
		}

		open fun onClick(event: clickEvent?): GuiPage {
			this.click = event
			return this
		}

		open fun onClose(event: closeEvent?): GuiPage {
			this.close = event
			return this
		}

		open fun onOpen(event: openEvent?): GuiPage {
			this.open = event
			return this
		}

		open fun onDrag(event: dragEvent?): GuiPage {
			this.drag = event
			return this
		}
	}

	fun getItem(item: ItemStack, name: String?, vararg lore: String?): ItemStack {
		val im = item.itemMeta
		if (name != null) im.setDisplayName(color(name)[0])
		im.lore = Arrays.asList(*color(*lore))
		item.setItemMeta(im)
		return item
	}

	fun getItem(matchMaterial: Material, name: String?, vararg lore: String?): ItemStack {
		return getItem(ItemStack(matchMaterial), name, *lore)
	}

	@Suppress("deprecation")
	fun getItem(matchMaterial: Material, damage: Short, amount: Int, name: String?, vararg lore: String?): ItemStack {
		val item = getItem(matchMaterial, name, *lore)
		item.amount = amount
		item.durability = damage
		return item
	}

	fun color(vararg strings: String?): Array<String?> {
		val s = arrayOfNulls<String>(strings.size)
		for (i in strings.indices) {
			if (strings[i] != null) s[i] = ChatColor.translateAlternateColorCodes(
				'&',
				strings[i]!!
			)
		}
		return s
	}

	fun color(strings: List<String>): Array<String?> {
		return color(*strings.toTypedArray<String>())
	}

	fun getInventory(name: String, size: Int): Inventory {
		return if (size == 1) {
			Bukkit.createInventory(null, InventoryType.DROPPER, name)
		} else if (size == 5) {
			Bukkit.createInventory(null, InventoryType.HOPPER, name)
		} else {
			Bukkit.createInventory(
				null,
				min(54.0, max(9.0, (ceil((size / 9).toDouble()).toInt() * 9).toDouble())).toInt(),
				name
			)
		}
	}

	fun interface clickEvent {
		fun click(e: InventoryClickEvent?)
	}

	fun interface closeEvent {
		fun close(e: InventoryCloseEvent?)
	}

	interface dragEvent {
		fun drag(e: InventoryDragEvent?)
	}

	interface openEvent {
		fun open(e: InventoryOpenEvent?)
	}

	companion object {
		// Macro for getting a subarray
		fun <T> subArray(array: Array<T>, beg: Int, end: Int): Array<T> {
			return array.copyOfRange(beg, minOf(end + 1, array.size)) // prevent out-of-bounds
		}

	}
}