package net.crewco.PolicePlugin.commands

import com.google.inject.Inject
import net.crewco.PolicePlugin.Startup
import net.crewco.PolicePlugin.Startup.Companion.messagesManager
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission


class prisonCommand @Inject constructor(private val plugin:Startup){
	@Command("prison-reload")
	@Permission("prision.reload")
	fun onReload(player:Player){
		plugin.load()
		player.sendMessage(messagesManager.messages.get("config-reloaded")!!)
	}
}