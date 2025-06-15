package net.crewco.PolicePlugin.Util

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.util.*


class Util {
	fun log(sender: Player, message: String) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message))
	}

	fun color(s: String): String {
		return ChatColor.translateAlternateColorCodes('&', s)
	}

	fun color(s: List<String>): List<String> {
		val colored: MutableList<String> = ArrayList()
		for (color in s) colored.add(ChatColor.translateAlternateColorCodes('&', color))
		return colored
	}

	fun getSkull(p: Player): ItemStack {
		val skull = ItemStack(Material.PLAYER_HEAD)
		val skullMeta = skull.itemMeta as SkullMeta
		skullMeta.setDisplayName(p.displayName)
		skullMeta.setOwningPlayer(p)
		skull.setItemMeta(skullMeta)
		return skull
	}

	@SafeVarargs
	fun colorList(s: List<String>, vararg pair: Pair<String, String>): List<String> {
		val colored: MutableList<String> = ArrayList()
		for (color in s) {
			colored.add(
				ChatColor.translateAlternateColorCodes(
					'&',
					replaceAll(color, *pair)
				)
			)
		}
		return colored
	}

	@SafeVarargs
	fun replaceAll(s: String, vararg pairs: Pair<String, String>): String {
		var result = s
		for (p in pairs) {
			result = result.replace(Regex(p.key), p.value)
		}
		return result
	}

	fun color(vararg strings: String?): Array<String?> {
		val s = arrayOfNulls<String>(strings.size)
		for (i in strings.indices) {
			if (strings[i] != null) s[i] = ChatColor.translateAlternateColorCodes('&', strings[i]!!)
		}
		return s
	}

	fun <T> subArray(array: Array<T>, beg: Int, end: Int): Array<T> {
		return Arrays.copyOfRange(array, beg, end + 1)
	}

	fun getItem(item: ItemStack, name: String?, lore: List<String>): ItemStack {
		val im = item.itemMeta
		if (name != null) im.setDisplayName(color(name))
		im.lore = color(lore)
		item.setItemMeta(im)
		return item
	}

	fun getItem(item: ItemStack, name: String?, p: Pair<String, String>): ItemStack {
		val im = item.itemMeta
		if (name != null) im.setDisplayName(color(name))
		im.persistentDataContainer.set(NamespacedKey.minecraft(p.key), PersistentDataType.STRING, p.value)
		item.setItemMeta(im)
		return item
	}

	fun getItem(item: ItemStack, name: String, lore: List<String>, p: Pair<String, String>): ItemStack {
		val im = item.itemMeta
		if (name != null) im.setDisplayName(color(name))
		im.lore = color(lore)
		im.persistentDataContainer.set(NamespacedKey.minecraft(p.key), PersistentDataType.STRING, p.value)
		im.addItemFlags(*arrayOf(ItemFlag.HIDE_ENCHANTS))
		item.setItemMeta(im)
		return item
	}


}