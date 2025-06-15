package net.crewco.PolicePlugin.Util

import com.google.inject.Inject
import net.crewco.PolicePlugin.Startup
import org.bukkit.ChatColor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

class Messages @Inject constructor(private val plugin:Startup)  {
	private lateinit var messagesFile: File

	private lateinit var config: FileConfiguration

	var messages: MutableMap<String, String> = mutableMapOf()

	var help: List<String> = ArrayList()

	fun Messages() { loadFile() }

	fun getFile() : FileConfiguration { return config }

	fun loadFile() {
		if (messages.isNotEmpty()){ messages.clear()}
		messagesFile = File(plugin.dataFolder, "messages.yml")
		if (!messagesFile.exists()){plugin.saveResource("messages.yml", false)}
		config = YamlConfiguration.loadConfiguration(messagesFile)
		try {
			config.save(messagesFile)
		} catch (e: IOException) {
			e.printStackTrace()
		}
		for (s in config.getConfigurationSection("messages")
			?.getKeys(false)!!) messages[s] = config.getString(
			"messages.$s"
		)?.let {
			ChatColor.translateAlternateColorCodes(
				'&', it
			)
		}.toString()
		help = Util().color(config.getStringList("help"))
	}
}