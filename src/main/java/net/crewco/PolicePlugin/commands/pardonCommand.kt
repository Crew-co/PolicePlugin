package net.crewco.PolicePlugin.commands

import net.crewco.PolicePlugin.Startup.Companion.jailManager
import net.crewco.PolicePlugin.Startup.Companion.messagesManager
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission

class pardonCommand {
	@Command("pardon <target>")
	@Permission("prison.pardon")
	fun onPardon(player:Player, @Argument("target") target:Player){
		if (jailManager.isPrisoned(target)){
			jailManager.releasePlayer(target)
			target.sendMessage((messagesManager.messages["been-released"] as String).replace("%player%".toRegex(), player.name))
			player.sendMessage((messagesManager.messages["released-player"] as String).replace("%player%".toRegex(), target.name))
		}else{
			player.sendMessage(messagesManager.messages.get("invalid-player")!!)
		}
	}

}