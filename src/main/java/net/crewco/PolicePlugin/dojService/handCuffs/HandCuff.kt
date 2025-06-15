package net.crewco.PolicePlugin.dojService.handCuffs

import com.google.inject.Inject
import net.crewco.PolicePlugin.Startup
import net.crewco.PolicePlugin.Startup.Companion.utilsManager
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class HandCuff @Inject constructor(private val plugin:Startup) {
	var cuffed: MutableMap<Player, Player> = HashMap()

	fun getHandcuff(): ItemStack? {
		if (plugin.config.isSet("handcuff")) {
			val mat =
				plugin.config.getString("handcuff.material")?.let { Material.getMaterial(it.toUpperCase()) } ?: return null
			val item = ItemStack(mat)
			val meta = item.itemMeta
			if (plugin.config.isSet("handcuff.name")){
				meta.setDisplayName(utilsManager.color(plugin.config.getString("handcuff.name")!!))
			}
			if (plugin.config.isSet("handcuff.lore")){
				meta.lore = utilsManager.color(plugin.config.getStringList("handcuff.lore"))
			}
			if (plugin.config.isSet("handcuff.modelData")){
				meta.setCustomModelData(plugin.config.getInt("handcuff.modelData"))
			}
			if (plugin.config.isSet("handcuff.glow") && plugin.config.getBoolean("handcuff.glow")) {
				meta.addEnchant(Enchantment.MENDING, 1, false)
				meta.addItemFlags(*arrayOf(ItemFlag.HIDE_ENCHANTS))
			}
			meta.persistentDataContainer.set(NamespacedKey.minecraft("handcuff"), PersistentDataType.BYTE, 1.toByte())
			item.setItemMeta(meta)
			return item
		}
		return null
	}

	fun getCuffer(player: Player): Player? {
		return cuffed.getOrDefault(player, null)
	}

	fun getCuffed(player: Player): Player? {
		if (cuffed.containsValue(player)) return null
		return cuffed.entries
			.filter { it.value == player }
			.associate { it.value to it.key }
			.getOrDefault(player, null)
	}

	fun getCuffed(): MutableMap<Player, Player> {
		return cuffed
	}

	fun holdingCuff(p: Player): Boolean {
		val item = p.equipment.itemInMainHand
		return (item.hasItemMeta() && item.itemMeta.persistentDataContainer
			.has(NamespacedKey.minecraft("handcuff"), PersistentDataType.BYTE))
	}

	fun isCuffed(p: Player?): Boolean {
		return cuffed.containsKey(p)
	}

	fun cuffPlayer(cuffer: Player, target: Player) {
		cuffed[target] = cuffer
		target.allowFlight = true
	}

	fun uncuffPlayer(player: Player) {
		if (cuffed.containsKey(player)) {
			cuffed.remove(player)
			player.allowFlight = false
		}
	}
}