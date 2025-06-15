package net.crewco.PolicePlugin.commands

import com.google.inject.Inject
import net.crewco.PolicePlugin.Startup
import net.crewco.PolicePlugin.Startup.Companion.messagesManager
import net.crewco.PolicePlugin.Startup.Companion.sysMsg
import net.crewco.PolicePlugin.Startup.Companion.utilsManager
import net.crewco.PolicePlugin.Startup.Companion.wantedListManager
import net.crewco.PolicePlugin.guis.dojguis.WantedGui
import org.apache.commons.lang.math.NumberUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import java.util.*

class wantedlistCommand @Inject constructor(private val plugin:Startup) {
	@Command("wantedlist <args>")
	@Permission("prison.wl.use")
	fun onExec(player:Player,@Argument("args") args:Array<String>){
		when (args[0]){
			"view" -> {
				if (player.hasPermission("prison.wl.help")){
					WantedGui(plugin).view(player)
				}
			}

			"add" -> {
				if (player.hasPermission("prison.wl.add")) {
					if (args.size > 3 + plugin.config.getInt("settings.word-limit") || Arrays
							.stream(utilsManager.subArray(args, 3, args.size - 1))
							.reduce("") { now, old -> "$now $old" }
							.strip().length > plugin.config.getInt("settings.character-limit")
					) {
						player.sendMessage(messagesManager.messages.get("args-length")!!)
						return
					}
					if (Bukkit.getPlayer(args[1]) == null) {
						player.sendMessage(messagesManager.messages.get("invalid-command")!!)
						return
					}
					if (!NumberUtils.isDigits(args[2])) {
						player.sendMessage(messagesManager.messages.get("invalid-command")!!)
						return
					}
					if (wantedListManager.getPlayers().containsKey(Bukkit.getPlayer(args[1])!!.uniqueId)) {
						player.sendMessage(messagesManager.messages.get("player-wanted")!!)
						return
					}
					wantedListManager.addPlayer(
						Bukkit.getPlayer(args[1])!!.uniqueId, player.uniqueId,
						NumberUtils.toInt(args[2]), Arrays.stream(utilsManager.subArray(args, 3, args.size - 1))
							.reduce("") { now, old -> "$now $old" }.strip()
					)
					player.sendMessage(messagesManager.messages["wanted-add"]!!)
				} else {
					player.sendMessage(messagesManager.messages["invalid-permission"]!!)
				}
			}
			"update" -> {
				if (player.hasPermission("prison.wl.update")) {
					if (args.size > 3 + plugin.config.getInt("settings.word-limit") || Arrays
							.stream(utilsManager.subArray(args, 3, args.size - 1))
							.reduce("") { now, old -> "$now $old" }
							.strip().length > plugin.getConfig().getInt("settings.character-limit")
					) {
						player.sendMessage(messagesManager.messages.get("args-length")!!)
						return
					}
					if (Bukkit.getPlayer(args[1]) == null) {
						player.sendMessage(messagesManager.messages.get("invalid-command")!!)
						return
					}
					if (!NumberUtils.isDigits(args[2])) {
						player.sendMessage(messagesManager.messages.get("invalid-command")!!)
						return
					}
					if (!wantedListManager.getPlayers().containsKey(Bukkit.getPlayer(args[1])!!.uniqueId)) {
						player.sendMessage(messagesManager.messages.get("player-unwanted")!!)
						return
					}
					wantedListManager.updatePlayer(
						Bukkit.getPlayer(args[1])!!.uniqueId, player.uniqueId,
						NumberUtils.toInt(args[2]), Arrays.stream(utilsManager.subArray(args, 3, args.size - 1))
							.reduce("") { now, old -> "$now $old" }.strip()
					)
					player.sendMessage(messagesManager.messages.get("wanted-update")!!)
				} else {
					player.sendMessage(messagesManager.messages.get("invalid-permission")!!)
				}
			}

			"remove" -> {
				if (player.hasPermission("prison.wl.remove")) {
					if (Bukkit.getPlayer(args[1]) == null) {
						player.sendMessage(messagesManager.messages.get("invalid-command")!!)
						return
					}
					if (!wantedListManager.getPlayers().containsKey(Bukkit.getPlayer(args[1])!!.uniqueId)) {
						player.sendMessage(messagesManager.messages.get("player-unwanted")!!)
						return
					}
					wantedListManager.guiRemovePlayer(player, Bukkit.getPlayer(args[1])!!.uniqueId)
					player.sendMessage(messagesManager.messages.get("wanted-remove")!!)
				} else {
					player.sendMessage(messagesManager.messages.get("invalid-permission")!!)
				}
			}
			else  -> {
				player.sendMessage("${sysMsg}Command uage: /wantedlist <update|add|remove|view>")
			}
		}
	}
}