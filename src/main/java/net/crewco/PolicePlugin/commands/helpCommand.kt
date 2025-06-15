package net.crewco.PolicePlugin.commands

import net.crewco.PolicePlugin.Startup.Companion.messagesManager
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command

class helpCommand {
	@Command("prison-help")
	fun onHelp(player: Player){
		for (s in messagesManager.help){ player.sendMessage(s)}
	}
}