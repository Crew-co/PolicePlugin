package net.crewco.PolicePlugin.commands

import net.crewco.PolicePlugin.Startup.Companion.handCuffManager
import net.crewco.PolicePlugin.Startup.Companion.messagesManager
import net.crewco.PolicePlugin.Startup.Companion.sysMsg
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission

class cuffCommand {
	@Command("cuff <target>")
	@Permission("prison.cuff")
	fun onCuff(player:Player, @Argument("target") target: Player){
		if (target == player) {player.sendMessage("${sysMsg}You Can not Cuff your self");return}
		handCuffManager.cuffPlayer(player,target)
		target.sendMessage(
			(messagesManager.messages["cuffing-success"] as String).replace(
				"%player%".toRegex(),
				player.name
			)
		)
		player.sendMessage(
			(messagesManager.messages["cuffer-success"] as String).replace(
				"%player%".toRegex(),
				target.name
			)
		)
	}

}