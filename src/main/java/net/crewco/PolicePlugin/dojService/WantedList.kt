package net.crewco.PolicePlugin.dojService

import com.google.inject.Inject
import net.crewco.PolicePlugin.Startup
import net.crewco.PolicePlugin.Startup.Companion.messagesManager
import net.crewco.PolicePlugin.Startup.Companion.utilsManager
import net.crewco.PolicePlugin.Util.Pair
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.io.File
import java.io.IOException
import java.util.*

class WantedList @Inject constructor(private val plugin:Startup){
	private var file: File? = null
	lateinit var config: FileConfiguration
	private val players: MutableMap<UUID, WantedPlayer> = HashMap()
	private val update: MutableMap<UUID, WantedPlayer> = HashMap()
	private var template: List<String>? = null

	fun getcfg(): FileConfiguration {
		return config
	}

	fun addPlayer(uuid: UUID, who: UUID, priority: Int, reason: String?) {
		players[uuid] = WantedPlayer(uuid, who, priority, reason!!)
		config["players.$uuid.priority"] = priority
		config["players.$uuid.who"] = who.toString()
		config["players.$uuid.reason"] = reason
		if (config!!.isSet("broadcast") && config.getBoolean("broadcast")) Bukkit.broadcastMessage(
			utilsManager.replaceAll(
				messagesManager.messages["wanted-added"]!!,
				Pair.of("%player%", Bukkit.getOfflinePlayer(uuid).name!!),
				Pair.of("%who%", Bukkit.getOfflinePlayer(who).name!!), Pair.of("%priority%", "" + priority),
				Pair.of("%reason%", reason)
			)
		)
		else Bukkit.getOnlinePlayers().stream().filter { o: Player? -> o!!.hasPermission("prison.wl.broadcast.added") }
			.forEach { o: Player? ->
				o?.sendMessage(
					utilsManager.replaceAll(
						messagesManager.messages["wanted-added"]!!,
						Pair.of("%player%", Bukkit.getOfflinePlayer(uuid).name!!),
						Pair.of("%who%", Bukkit.getOfflinePlayer(who).name!!),
						Pair.of("%priority%", "" + priority), Pair.of("%reason%", reason)
					)
				)
			}
		save()
	}

	fun updatePlayer(uuid: UUID, who: UUID, priority: Int, reason: String?) {
		val p = players[uuid]
		p!!.setPriority(priority)
		p.setWho(who)
		p.setReason(reason)
		config!!["players.$uuid.priority"] = priority
		config!!["players.$uuid.who"] = who.toString()
		config!!["players.$uuid.reason"] = reason
		if (config!!.isSet("broadcast") && config!!.getBoolean("broadcast")) Bukkit.broadcastMessage(
			utilsManager.replaceAll(
				messagesManager.messages.get("wanted-updated")!!,
				Pair.of("%player%", Bukkit.getOfflinePlayer(uuid).name!!),
				Pair.of("%who%", Bukkit.getOfflinePlayer(who).name!!), Pair.of("%priority%", "" + priority),
				Pair.of("%reason%", reason!!)
			)
		)
		else Bukkit.getOnlinePlayers().stream().filter { o: Player? -> o!!.hasPermission("prison.wl.broadcast.update") }
			.forEach { o: Player? ->
				o?.sendMessage(
					utilsManager.replaceAll(
						messagesManager.messages.get("wanted-updated")!!,
						Pair.of("%player%", Bukkit.getOfflinePlayer(uuid).name!!),
						Pair.of("%who%", Bukkit.getOfflinePlayer(who).name!!),
						Pair.of("%priority%", "" + priority), Pair.of("%reason%", reason!!)
					)
				)
			}
		save()
	}

	fun removePlayer(uuid: UUID) {
		players.remove(uuid)
		config!!["players.$uuid"] = null
		save()
	}

	fun guiRemovePlayer(p: Player, uuid: UUID) {
		val player = Bukkit.getOfflinePlayer(uuid)
		if (config!!.isSet("broadcast") && config!!.getBoolean("broadcast")) Bukkit.broadcastMessage(
			utilsManager.replaceAll(
				messagesManager.messages.get("wanted-removed")!!,
				Pair.of("%player%", player.name!!), Pair.of("%who%", p.name)
			)
		)
		else Bukkit.getOnlinePlayers().stream()
			.filter { o: Player? -> o!!.hasPermission("prison.wl.broadcast.removed") }
			.forEach { o: Player? ->
				o?.sendMessage(
					utilsManager.replaceAll(
						messagesManager.messages.get("wanted-removed")!!,
						Pair.of("%player%", player.name!!), Pair.of("%who%", p.name)
					)
				)
			}
		removePlayer(uuid)
	}

	fun removePlayer(p: Player, uuid: UUID) {
		val wp = players[uuid]
		if (config!!.isSet("broadcast") && config!!.getBoolean("broadcast")) Bukkit.broadcastMessage(
			utilsManager.replaceAll(
				messagesManager.messages.get("wanted-arrested")!!,
				Pair.of("%player%", Bukkit.getOfflinePlayer(wp!!.getUuid()).name!!),
				Pair.of("%who%", p.name)
			)
		)
		else Bukkit.getOnlinePlayers().stream()
			.filter { o: Player? -> o!!.hasPermission("prison.wl.broadcast.arrested") }
			.forEach { o: Player? ->
				o?.sendMessage(
					utilsManager.replaceAll(
						messagesManager.messages.get("wanted-arrested")!!,
						Pair.of("%player%", Bukkit.getOfflinePlayer(wp!!.getUuid()).name!!),
						Pair.of("%who%", p.name)
					)
				)
			}
		removePlayer(uuid)
	}

	fun getPlayers(): Map<UUID, WantedPlayer> {
		return players
	}

	fun save() {
		try {
			config!!.save(file!!)
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	fun getHeads(): Array<ItemStack> {
		val heads = arrayOfNulls<ItemStack>(players.size)
		val pl: List<WantedPlayer> = players.values.stream().toList()
		for (i in 0..<players.size) {
			val player = pl[i]
			val skull = ItemStack(Material.PLAYER_HEAD, 1)
			val skullMeta = skull.itemMeta as SkullMeta
			val p = Bukkit.getOfflinePlayer(player.getUuid())
			val whom = Bukkit.getOfflinePlayer(player.getWho())
			skullMeta.setDisplayName(
				utilsManager.color(
					template!![0].replace("%player%".toRegex(),
						(if (p.player == null) p.name else p.player!!.displayName)!!
					))
			)
			skullMeta.setOwningPlayer(p)
			skullMeta.lore = utilsManager.colorList(
				template!!.subList(1, template!!.size), Pair.of("%player%", p.name!!),
				Pair.of("%who%", whom.name!!), Pair.of("%priority%", "" + player.getPriority()),
				Pair.of("%reason%", player.getReason())
			)
			skull.setItemMeta(skullMeta)
			heads[i] = skull
		}
		return heads.requireNoNulls()
	}

	fun loadFile() {
		file = File(plugin.dataFolder, "wanted-list.yml")
		if (!file!!.exists()) plugin.saveResource("wanted-list.yml", false)
		config = YamlConfiguration.loadConfiguration(file!!)
		try {
			config!!.save(file!!)
		} catch (e: IOException) {
			e.printStackTrace()
		}
		template = plugin.config.getStringList("head")
		if (config!!.isSet("players")) for (uuidString in config!!.getConfigurationSection("players")!!
			.getKeys(false)) {
			val uuid = UUID.fromString(uuidString)
			val who = UUID.fromString(config!!.getString("players.$uuidString.who"))
			val priority = config!!.getInt("players.$uuidString.priority")
			val reason = config!!.getString("players.$uuidString.reason")
			players[uuid] = WantedPlayer(uuid, who, priority, reason!!)
		}
	}

	fun getUpdate(): Map<UUID, WantedPlayer> {
		return update
	}

	fun addUpdate(uuid: UUID, p: WantedPlayer) {
		update[uuid] = p
	}

	fun removeUpdate(uuid: UUID) {
		update.remove(uuid)
	}

	fun getTemplate(): List<String>? {
		return template
	}

}