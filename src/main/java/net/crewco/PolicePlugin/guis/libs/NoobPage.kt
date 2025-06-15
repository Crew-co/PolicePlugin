package net.crewco.PolicePlugin.guis.libs

import net.crewco.PolicePlugin.Startup
import net.crewco.PolicePlugin.guis.listener.Gui
import net.crewco.PolicePlugin.guis.listener.Gui.ClickEvent
import net.crewco.PolicePlugin.guis.listener.Gui.CloseEvent
import net.crewco.PolicePlugin.guis.listener.Gui.DragEvent
import net.crewco.PolicePlugin.guis.listener.Gui.OpenEvent
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

// Christos Naming Version of GUIPage
class NoobPage : GuiPage {
	private val plugin = Startup.plugin
	private val gui = Gui(plugin)

	constructor(name: String, size: Int, vararg items: ItemStack) : super(name, size, *items)

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

	fun a(item: ItemStack): NoobPage {
		super.addItem(item)
		return this
	}

	fun a(item: Material): NoobPage {
		super.addItem(item)
		return this
	}

	fun a(item: ItemStack, name: String, vararg lore: String): NoobPage {
		super.addItem(gui.getItem(item, name, *lore))
		return this
	}

	fun a(item: Material, name: String, vararg lore: String): NoobPage {
		super.addItem(gui.getItem(ItemStack(item), name, *lore))
		return this
	}

	fun a(vararg items: ItemStack): NoobPage {
		super.addItems(*items)
		return this
	}

	fun a(vararg items: Material): NoobPage {
		super.addItems(*items)
		return this
	}

	fun i(position: Int, item: ItemStack): NoobPage {
		super.setItem(position, item)
		return this
	}

	fun i(position: Int, item: Material): NoobPage {
		super.setItem(position, item)
		return this
	}

	fun i(position: Int, item: ItemStack, name: String, vararg lore: String): NoobPage {
		super.setItem(position, gui.getItem(item, name, *lore))
		return this
	}

	fun i(position: Int, item: Material, name: String, vararg lore: String): NoobPage {
		super.setItem(position, gui.getItem(ItemStack(item), name, *lore))
		return this
	}

	fun i(row: Int, column: Int, item: ItemStack): NoobPage {
		super.setItem(column + row * 9, item)
		return this
	}

	fun i(row: Int, column: Int, item: Material): NoobPage {
		super.setItem(column + row * 9, item)
		return this
	}

	fun i(row: Int, column: Int, item: ItemStack, name: String, vararg lore: String): NoobPage {
		super.setItem(column + row * 9, gui.getItem(item, name, *lore))
		return this
	}

	fun i(row: Int, column: Int, item: Material, name: String, vararg lore: String): NoobPage {
		super.setItem(column + row * 9, gui.getItem(ItemStack(item), name, *lore))
		return this
	}

	fun i(item: ItemStack, vararg slots: Int): NoobPage {
		super.setItems(item, *slots)
		return this
	}

	fun i(item: Material, vararg slots: Int): NoobPage {
		super.setItems(item, *slots)
		return this
	}

	fun i(slots: List<Int>, item: ItemStack): NoobPage {
		super.setItems(slots, item)
		return this
	}

	fun i(slots: List<Int>, item: Material): NoobPage {
		super.setItems(slots, item)
		return this
	}

	fun i(slots: List<Int>, item: ItemStack, name: String, vararg lore: String): NoobPage {
		super.setItems(slots, item, name, *lore)
		return this
	}

	fun i(slots: List<Int>, item: Material, name: String, vararg lore: String): NoobPage {
		super.setItems(slots, item, name, *lore)
		return this
	}

	fun fc(column: Int, item: ItemStack): NoobPage {
		super.fillColumn(column, item)
		return this
	}

	fun fc(column: Int, item: Material): NoobPage {
		super.fillColumn(column, ItemStack(item))
		return this
	}

	fun fc(column: Int, item: ItemStack, name: String, vararg lore: String): NoobPage {
		super.fillColumn(column, gui.getItem(item, name, *lore))
		return this
	}

	fun fc(column: Int, item: Material, name: String, vararg lore: String): NoobPage {
		super.fillColumn(column, gui.getItem(ItemStack(item), name, *lore))
		return this
	}

	fun fr(row: Int, item: ItemStack): NoobPage {
		super.fillRow(row, item)
		return this
	}

	fun fr(row: Int, item: Material): NoobPage {
		super.fillRow(row, item)
		return this
	}

	fun fr(row: Int, item: ItemStack, name: String, vararg lore: String): NoobPage {
		super.fillRow(row, item, name, *lore)
		return this
	}

	fun fr(row: Int, item: Material, name: String, vararg lore: String): NoobPage {
		super.fillRow(row, item, name, *lore)
		return this
	}

	fun f(item: ItemStack): NoobPage {
		super.fill(item)
		return this
	}

	fun f(item: Material): NoobPage {
		super.fill(item)
		return this
	}

	fun f(item: ItemStack, name: String, vararg lore: String): NoobPage {
		super.fill(gui.getItem(item, name, *lore))
		return this
	}

	fun f(item: Material, name: String, vararg lore: String): NoobPage {
		super.fill(gui.getItem(ItemStack(item), name, *lore))
		return this
	}

	fun onClick(event: ClickEvent): NoobPage {
		super.onClick(event)
		return this
	}

	fun onClose(event: CloseEvent): NoobPage {
		super.onClose(event)
		return this
	}

	fun onOpen(event: OpenEvent): NoobPage {
		super.onOpen(event)
		return this
	}

	fun onDrag(event: DragEvent): NoobPage {
		super.onDrag(event)
		return this
	}
}