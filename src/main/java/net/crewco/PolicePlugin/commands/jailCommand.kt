package net.crewco.PolicePlugin.commands

import net.crewco.PolicePlugin.Startup.Companion.jailManager
import net.crewco.PolicePlugin.Startup.Companion.messagesManager
import net.crewco.PolicePlugin.Startup.Companion.utilsManager
import net.crewco.PolicePlugin.Startup.Companion.wantedListManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import java.util.*

class jailCommand {
	@Command("jail <target> <time> <reason>")
	@Permission("prison.jail")
	fun onJail(player:Player, @Argument("target") target:Player,@Argument("time") time:Int,@Argument("reason") reason:Array<String>){
		if (reason.isEmpty()){ reason[0] = "No Reason Specified" }

		// Info
		val lore: MutableList<String> = ArrayList()
		lore.add(utilsManager.color("&7&lOfficer: &f" + player.name))
		lore.add(utilsManager.color("&7&lArrested: &f" + target.name))
		lore.add(utilsManager.color("&7&lDate: &f" + (Date()).toString()))
		lore.add(utilsManager.color("&7&lCharges:"))
		lore.add("&b- &c$reason")
		val total = time * 60
		val timeS: String = utilsManager.color(
			if (total / 60 != 0)
				(((total / 60).toString() + "m"
						+ (if (total % 60 > 0) ((total % 60).toString() + "s") else "")))
			else
				((total % 60).toString() + "s")
		)
		lore.add(utilsManager.color("&7&lTime: &f$timeS"))
		val slip: ItemStack = utilsManager.getItem(ItemStack(Material.PAPER), "&bJail File", lore)
		player.inventory.addItem(slip)
		if (wantedListManager.getPlayers().containsKey(target.uniqueId)){
			wantedListManager.removePlayer(target.uniqueId)
		}
		jailManager.imprisonPlayer(target, jailManager.getPrisons()[0],total)
		target.inventory.addItem(slip)
		target.sendMessage((messagesManager.messages["prisoner"] as String).replace("%player%".toRegex(), player.name).replace("%time%".toRegex(), time.toString()))
		player.sendMessage((messagesManager.messages["prisoned"] as String).replace("%player%".toRegex(),target.name).replace("%time%".toRegex(), time.toString()))


	}
}