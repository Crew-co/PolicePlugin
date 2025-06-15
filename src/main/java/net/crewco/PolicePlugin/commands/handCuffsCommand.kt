package net.crewco.PolicePlugin.commands

import net.crewco.PolicePlugin.Startup.Companion.handCuffManager
import net.crewco.PolicePlugin.Startup.Companion.messagesManager
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission

class handCuffsCommand {
	@Command("handcuffs")
	@Permission("prison.handcuffs.give")
	fun onGetCuff(player:Player){
		player.inventory.addItem(handCuffManager.getHandcuff()!!)
		player.sendMessage(messagesManager.messages.get("given-cuff")!!)
	}
}