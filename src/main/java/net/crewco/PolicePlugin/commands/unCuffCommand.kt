package net.crewco.PolicePlugin.commands

import net.crewco.PolicePlugin.Startup.Companion.handCuffManager
import net.crewco.PolicePlugin.Startup.Companion.messagesManager
import net.crewco.PolicePlugin.Startup.Companion.sysMsg
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.suggestion.Suggestions
import org.incendo.cloud.context.CommandContext
import java.util.stream.Stream

class unCuffCommand {
	@Command("uncuff <target>")
	@Permission("prison.uncuff")
	fun onUnCuff(player:Player, @Argument("target") target: Player){
		if (target == player) {player.sendMessage("${sysMsg}You Can not Uncuff your self");return}
		handCuffManager.uncuffPlayer(target)
		target.sendMessage(
			(messagesManager.messages["cuffing-release-success"] as String)
				.replace("%player%".toRegex(), player.name)
		)
		player.sendMessage(
			(messagesManager.messages["cuffer-release-success"] as String).replace(
				"%player%".toRegex(),
				target.name
			)
		)
	}
}