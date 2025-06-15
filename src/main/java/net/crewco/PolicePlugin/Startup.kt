package net.crewco.PolicePlugin


import net.crewco.PolicePlugin.Util.Messages
import net.crewco.PolicePlugin.Util.Util
import net.crewco.PolicePlugin.commands.cuffCommand
import net.crewco.PolicePlugin.commands.handCuffsCommand
import net.crewco.PolicePlugin.commands.jailCommand
import net.crewco.PolicePlugin.commands.pardonCommand
import net.crewco.PolicePlugin.commands.prisonCommand
import net.crewco.PolicePlugin.commands.unCuffCommand
import net.crewco.PolicePlugin.commands.wantedlistCommand
import net.crewco.PolicePlugin.docService.Jail
import net.crewco.PolicePlugin.docService.JailListener
import net.crewco.PolicePlugin.dojService.WantedList
import net.crewco.PolicePlugin.dojService.handCuffs.HandCuff
import net.crewco.PolicePlugin.dojService.handCuffs.HandcuffListener
import net.crewco.PolicePlugin.guis.SearchGui
import net.crewco.PolicePlugin.guis.docguis.PrisionGui
import net.crewco.PolicePlugin.guis.listener.Gui
import net.crewco.common.CrewCoPlugin
import net.jpountz.util.Utils
import org.bukkit.ChatColor
import java.util.*
import kotlin.properties.Delegates

class Startup : CrewCoPlugin() {
	companion object{
		lateinit var plugin:Startup
			private set
		lateinit var whitelistManager:MutableList<String>
		lateinit var jailManager: Jail
		lateinit var messagesManager: Messages
		lateinit var utilsManager: Util
		lateinit var wantedListManager:WantedList
		lateinit var handCuffManager: HandCuff
		lateinit var prisionGui: PrisionGui
		lateinit var searchGui: SearchGui
		lateinit var sysMsg:String
	}

	var maxtime: Int = 0
	var cufftimer: Int = 0

	override suspend fun onEnableAsync() {
		super.onEnableAsync()

		//System Inits
		plugin = this
		messagesManager = Messages(this)
		utilsManager = Util()
		sysMsg = ChatColor.translateAlternateColorCodes('&',"&7[&1Justice&7]> ")
		load()

		//Load DOC System
		jailManager = Jail(this)

		//Load  DOJ System
		wantedListManager = WantedList(this)
		handCuffManager = HandCuff(this)

		//Config
		plugin.reloadConfig()
		plugin.config.options().copyDefaults(true)
		plugin.saveDefaultConfig()

		// DOC Listeners
		registerListeners(JailListener::class)

		// DOJ Listeners
		registerListeners(HandcuffListener::class)

		// System Listeners
		registerListeners(Gui::class)

		//Doj Commands
		registerCommands(handCuffsCommand::class,cuffCommand::class,unCuffCommand::class)

		//Doc Commands
		registerCommands(pardonCommand::class,jailCommand::class)

		//Global
		registerCommands(wantedlistCommand::class,prisonCommand::class)


	}

	override suspend fun onDisableAsync() {
		super.onDisableAsync()
	}


fun load() {
		jailManager.loadFile()
		messagesManager.loadFile()
		wantedListManager.loadFile()
		prisionGui = PrisionGui(this)
		searchGui = SearchGui(this)
		maxtime = config.getInt("settings.logoff-time")
		cufftimer = config.getInt("settings.cuff-timer")
		whitelistManager = config.getStringList("settings.prison-whitelist")
	}

}