package net.crewco.PolicePlugin.guis.docguis

import com.google.inject.Inject
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import net.crewco.PolicePlugin.Startup
import net.crewco.PolicePlugin.Startup.Companion.handCuffManager
import net.crewco.PolicePlugin.Startup.Companion.jailManager
import net.crewco.PolicePlugin.Startup.Companion.utilsManager
import net.crewco.PolicePlugin.Startup.Companion.wantedListManager
import net.crewco.PolicePlugin.Util.Pair
import net.crewco.PolicePlugin.Util.XMaterial
import net.crewco.PolicePlugin.guis.libs.NoobPage
import net.crewco.PolicePlugin.guis.listener.Gui
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class PrisionGui @Inject constructor(private val plugin:Startup){
	private var title: String? = ""
	private var size: Int = 27
	private val inventory: MutableMap<Pair<Int, Int>, ItemStack> = HashMap()
	private var fill: ItemStack = ItemStack(Material.AIR)

	init {
		val conf: FileConfiguration = plugin.config
		title = conf.getString("gui.title")
		size = conf.getInt("gui.size")
		for (s in conf.getConfigurationSection("gui.items")!!.getKeys(false)) {
			val mat = conf.getString("gui.items.$s.material")
			val item: ItemStack? = if (mat!!.contains(":")) {
				XMaterial.fromStringWithData(
					mat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0],
					mat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toByte()
				)
					.parseItem()
			} else {
				XMaterial.matchXMaterial(mat).get().parseItem()
			}
			if (s.contains("-")) {
				val nums = s.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
				val row = nums[0].toInt()
				val pos = nums[1].toInt()
				inventory[Pair(row, pos)] = utilsManager.getItem(
					item!!, conf.getString("gui.items.$s.name")!!,
					conf.getStringList("gui.items.$s.lore"),
					Pair("time", conf.getString("gui.items.$s.time")!!)
				)
				continue
			}
			if (s.equals("fill", ignoreCase = true)) {
				fill = utilsManager.getItem(
					item!!, conf.getString("gui.items.$s.name"),
					conf.getStringList("gui.items.$s.lore")
				)
				continue
			}
			if (s.equals("confirm", ignoreCase = true)) {
				val nums = conf.getString("gui.items.$s.position")!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }
					.toTypedArray()
				val row = nums[0].toInt()
				val pos = nums[1].toInt()
				inventory[Pair(row, pos)] = utilsManager.getItem(
					item!!,
					conf.getString("gui.items.$s.name"), Pair("confirm", "true")
				)
				continue
			}
			if (s.equals("close", ignoreCase = true)) {
				val nums = conf.getString("gui.items.$s.position")!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }
					.toTypedArray()
				val row = nums[0].toInt()
				val pos = nums[1].toInt()
				inventory[Pair(row, pos)] = utilsManager.getItem(
					item!!,
					conf.getString("gui.items.$s.name"), Pair("close", "true")
				)
			}
		}
	}

	fun create(selector: Player, target: Player, prison: ProtectedRegion?): Gui {
		val total = IntArray(1)
		val lore: MutableList<String> = ArrayList()
		lore.add(utilsManager.color("&7&lOfficer: &f" + selector.name))
		lore.add(utilsManager.color("&7&lArrested: &f" + target.name))
		lore.add(utilsManager.color("&7&lDate: &f" + (Date()).toString()))
		lore.add(utilsManager.color("&7&lCharges:"))
		val gui = Gui(plugin)
		val page: NoobPage = gui.create(title!!, size)
		page.fill(fill)
		for ((key, value) in inventory.entries) {
			page.setItem(key.key, key.value, value)
		}
		page.onClick { e ->
			e!!.isCancelled = true
			if (e.currentItem != null) {
				if (e.currentItem!!.hasItemMeta() && e.currentItem!!.itemMeta
						.persistentDataContainer
						.has(NamespacedKey.minecraft("time"), PersistentDataType.STRING)
				) {
					val value =
						e.currentItem!!.itemMeta.persistentDataContainer[NamespacedKey.minecraft("time"), PersistentDataType.STRING] as String
					val p = e.whoClicked as Player
					val stringTime = value.substring(0, value.length - 1).toInt()
					val time = if (value.contains("s"))
						stringTime
					else
						(if (value.contains("m"))
							(stringTime * 60)
						else
							(if (value.contains("h")) (stringTime * 3600) else stringTime))
					p.playSound(p.location, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f)
					if (!e.currentItem!!.itemMeta.hasEnchant(Enchantment.MENDING)) {
						e.currentItem!!.addUnsafeEnchantment(Enchantment.MENDING, 1)
						total[0] = total[0] + time
						lore.add(
							"&b- &c" + ChatColor.stripColor(
								e.currentItem!!.itemMeta.displayName
							)
						)
					} else {
						e.currentItem!!.removeEnchantment(Enchantment.MENDING)
						total[0] = total[0] - time
						lore.remove(
							"&b- &c" + ChatColor.stripColor(
								e.currentItem!!.itemMeta.displayName
							)
						)
					}
				} else if (e.currentItem!!.hasItemMeta() && e.currentItem!!.itemMeta
						.persistentDataContainer.has(NamespacedKey.minecraft("confirm"), PersistentDataType.STRING)
					&& total[0] > 0
				) {
					if (wantedListManager.getPlayers().containsKey(target.uniqueId)) wantedListManager.removePlayer(
						selector,
						target.uniqueId
					)
					jailManager.imprisonPlayer(target, prison!!, total[0])
					handCuffManager.getCuffed().remove(target)
					lore.add(
						utilsManager.color(
							"&7&lTime: &f"
									+ (if (total[0] / 60 != 0)
								((total[0] / 60).toString() + "m" + (total[0] % 60) + "s")
							else
								((total[0] % 60).toString() + "s"))
						)
					)
					val slip: ItemStack =
						utilsManager.getItem(ItemStack(Material.PAPER), "&bJail File", lore)
					target.inventory.addItem(slip)
					selector.inventory.addItem(slip)
					gui.close((e.whoClicked as Player))
				}
				if (e.currentItem!!.hasItemMeta() && e.currentItem!!.itemMeta
						.persistentDataContainer
						.has(NamespacedKey.minecraft("close"), PersistentDataType.STRING)
				) gui.close((e.whoClicked as Player))
			}
		}
		return gui
	}
}