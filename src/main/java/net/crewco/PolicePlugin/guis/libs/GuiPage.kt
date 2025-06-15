package net.crewco.PolicePlugin.guis.libs

import com.google.inject.Inject
import net.crewco.PolicePlugin.Startup
import net.crewco.PolicePlugin.guis.listener.Gui
import net.crewco.PolicePlugin.guis.listener.Gui.ClickEvent
import net.crewco.PolicePlugin.guis.listener.Gui.CloseEvent
import net.crewco.PolicePlugin.guis.listener.Gui.DragEvent
import net.crewco.PolicePlugin.guis.listener.Gui.OpenEvent
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

open class GuiPage{
	private val plugin = Startup.plugin
	private val gui = Gui(plugin)
	private val size: Int
	val page: Int
	private val name: String
	val i: Inventory
	private var click: ClickEvent? = null
	private var open: OpenEvent? = null
	private var close: CloseEvent? = null
	private var drag: DragEvent? = null
	var cancel = false
	var shift = false


	constructor(name: String, size: Int, vararg items: ItemStack) {
		this.name = name
		this.i = Gui(plugin).getInventory(name, size)
		this.size = i.size
		this.i.contents = items
		this.page = gui.size
	}

	constructor(template: GuiPage) {
		this.name = template.name
		this.i = gui.getInventory(template.name, template.size)
		this.size = this.i.size
		this.i.contents = template.getContents()
		this.page = gui.size
		this.cancel = template.cancel
		this.shift = template.shift
		this.onClick(template.getClick())
		this.onDrag(template.getDrag())
		this.onClose(template.getClose())
		this.onOpen(template.getOpen())
	}

	fun getName(): String {
		return name
	}

	fun getSize(): Int {
		return size
	}

	private fun getContents(): Array<ItemStack?> {
		return i.contents
	}

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

	fun addItem(item: ItemStack): GuiPage {
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
		val item = this.i.getItem(position)!!
		val meta = item.itemMeta!!
		meta.addEnchant(Enchantment.LURE, 1, true)
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
		item.itemMeta = meta
		this.i.setItem(position, item)
		return this
	}

	fun addGlow(row: Int, column: Int): GuiPage {
		val position = column + row * 9
		val item = this.i.getItem(position)!!
		val meta = item.itemMeta!!
		meta.addEnchant(Enchantment.LURE, 1, true)
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
		item.itemMeta = meta
		this.i.setItem(position, item)
		return this
	}

	fun removeGlow(position: Int): GuiPage {
		val item = this.i.getItem(position)!!
		val meta = item.itemMeta!!
		meta.removeEnchant(Enchantment.LURE)
		item.itemMeta = meta
		this.i.setItem(position, item)
		return this
	}

	fun removeGlow(row: Int, column: Int): GuiPage {
		val position = column + row * 9
		val item = this.i.getItem(position)!!
		val meta = item.itemMeta!!
		meta.removeEnchant(Enchantment.LURE)
		item.itemMeta = meta
		this.i.setItem(position, item)
		return this
	}

	fun addItem(item: ItemStack, name: String, vararg lore: String): GuiPage {
		return addItem(gui.getItem(item, name, *lore))
	}

	fun addItem(item: Material, name: String, vararg lore: String): GuiPage {
		return addItem(gui.getItem(ItemStack(item), name, *lore))
	}

	fun setItem(position: Int, item: ItemStack): GuiPage {
		i.setItem(position, item)
		return this
	}

	fun setItem(position: Int, item: Material): GuiPage {
		return setItem(position, ItemStack(item))
	}

	private fun setItem(position: Int, item: ItemStack, name: String, vararg lore: String): GuiPage {
		return setItem(position, gui.getItem(item, name, *lore))
	}

	fun setItem(position: Int, item: Material, name: String, vararg lore: String): GuiPage {
		return setItem(position, gui.getItem(ItemStack(item), name, *lore))
	}

	fun setItem(row: Int, column: Int, item: ItemStack): GuiPage {
		return setItem(column + row * 9, item)
	}

	fun setItem(row: Int, column: Int, item: Material): GuiPage {
		return setItem(column + row * 9, item)
	}

	fun setItem(row: Int, column: Int, item: ItemStack, name: String, vararg lore: String): GuiPage {
		return setItem(column + row * 9, gui.getItem(item, name, *lore))
	}

	fun setItem(row: Int, column: Int, item: Material, name: String, vararg lore: String): GuiPage {
		return setItem(column + row * 9, gui.getItem(ItemStack(item), name, *lore))
	}

	fun fillColumn(column: Int, item: ItemStack): GuiPage {
		for (i in 0 until 9) {
			if (i * 9 + column >= this.i.size)
				break
			if (this.i.contents[i * 9 + column] == null)
				setItem(i * 9 + column, item)
		}
		return this
	}

	fun fillColumn(column: Int, item: Material): GuiPage {
		return fillColumn(column, ItemStack(item))
	}

	fun fillColumn(column: Int, item: ItemStack, name: String, vararg lore: String): GuiPage {
		return fillColumn(column, gui.getItem(item, name, *lore))
	}

	fun fillColumn(column: Int, item: Material, name: String, vararg lore: String): GuiPage {
		return fillColumn(column, gui.getItem(ItemStack(item), name, *lore))
	}

	fun fillRow(row: Int, item: ItemStack): GuiPage {
		for (i in 0 until 9) {
			if (row * 9 + i >= this.i.size)
				break
			if (this.i.contents[row * 9 + i] == null)
				setItem(row * 9 + i, item)
		}
		return this
	}

	fun fillRow(row: Int, item: Material): GuiPage {
		return fillRow(row, ItemStack(item))
	}

	fun fillRow(row: Int, item: ItemStack, name: String, vararg lore: String): GuiPage {
		return fillRow(row, gui.getItem(item, name, *lore))
	}

	fun fillRow(row: Int, item: Material, name: String, vararg lore: String): GuiPage {
		return fillRow(row, gui.getItem(ItemStack(item), name, *lore))
	}

	fun addItems(vararg items: ItemStack): GuiPage {
		for (item in items)
			if (item != null)
				addItem(item)
		return this
	}

	fun addItems(vararg items: Material): GuiPage {
		for (item in items)
			if (item != null)
				addItem(item)
		return this
	}

	fun fill(item: ItemStack): GuiPage {
		for (i in 0 until this.i.size)
			if (this.i.contents[i] == null)
				setItem(i, item)
		return this
	}

	fun fill(item: Material): GuiPage {
		return fill(ItemStack(item))
	}

	fun fill(item: ItemStack, name: String, vararg lore: String): GuiPage {
		return fill(gui.getItem(item, name, *lore))
	}

	fun fill(item: Material, name: String, vararg lore: String): GuiPage {
		return fill(gui.getItem(ItemStack(item), name, *lore))
	}

	fun setItems(item: ItemStack, vararg slots: Int): GuiPage {
		for (i in slots.indices)
			this.i.setItem(slots[i], item)
		return this
	}

	fun setItems(item: Material, vararg slots: Int): GuiPage {
		return setItems(ItemStack(item), *slots)
	}

	fun setItems(slots: List<Int>, item: ItemStack): GuiPage {
		for (slot in slots)
			setItem(slot, item)
		return this
	}

	fun setItems(slots: List<Int>, item: Material): GuiPage {
		return setItems(slots, ItemStack(item))
	}

	fun setItems(slots: List<Int>, item: ItemStack, name: String, vararg lore: String): GuiPage {
		for (slot in slots)
			setItem(slot, item, name, *lore)
		return this
	}

	fun setItems(slots: List<Int>, item: Material, name: String, vararg lore: String): GuiPage {
		return setItems(slots, gui.getItem(ItemStack(item), name, *lore))
	}

	fun clearRow(row: Int): GuiPage {
		for (i in 0 until 9) {
			if (row * 9 + i >= this.i.size)
				break
			if (this.i.contents[row * 9 + i] != null)
				setItem(row * 9 + i, ItemStack(Material.AIR))
		}
		return this
	}

	fun onClick(event: ClickEvent?): GuiPage {
		this.click = event
		return this
	}

	fun onClose(event: CloseEvent?): GuiPage {
		this.close = event
		return this
	}

	fun onOpen(event: OpenEvent?): GuiPage {
		this.open = event
		return this
	}

	fun onDrag(event: DragEvent?): GuiPage {
		this.drag = event
		return this
	}

	fun getClick(): ClickEvent? {
		return click
	}

	fun getOpen(): OpenEvent? {
		return open
	}

	fun getClose(): CloseEvent? {
		return close
	}

	fun getDrag(): DragEvent? {
		return drag
	}
}