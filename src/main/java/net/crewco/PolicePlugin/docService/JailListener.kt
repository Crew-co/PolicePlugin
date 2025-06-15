package net.crewco.PolicePlugin.docService

import com.google.inject.Inject
import net.crewco.PolicePlugin.Startup
import net.crewco.PolicePlugin.Startup.Companion.jailManager
import net.crewco.PolicePlugin.Startup.Companion.messagesManager
import net.crewco.PolicePlugin.Startup.Companion.wantedListManager
import net.crewco.PolicePlugin.Startup.Companion.whitelistManager
import net.crewco.PolicePlugin.Util.Pair
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.PluginDisableEvent
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class JailListener @Inject constructor(private val plugin:Startup) : Listener {
	init{
		Bukkit.getScheduler().runTaskTimer((plugin), Runnable {
				for (p in jailManager.getPrisoners()) {
					if (jailManager.canRelease(p!!)) {
						jailManager.releasePlayer(p)
						continue
					}
					val seconds = Instant.now().until(jailManager.getPrisonerTime(p), ChronoUnit.SECONDS)
					p.spigot().sendMessage(
						ChatMessageType.ACTION_BAR,
						*(ComponentBuilder(
							(if (seconds / 60L > 0) (seconds / 60L).toString() + "m" else "") + (seconds % 60L) + "s"
						))
							.color(ChatColor.GREEN).create()
					)
					if (!jailManager.getPrisonerPrison(p).contains(
							p.location.blockX, p.location.blockY,
							p.location.blockZ
						)
					) jailManager.prisonerToCell(p)
				} }, 0L, 20L)
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onDisable(e: PluginDisableEvent?) {
		jailManager.savePrisonerData()
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onDisable(e: PlayerQuitEvent) {
		if (jailManager.isPrisoned(e.player)) {
			val p = e.player
			val seconds = Instant.now().until(jailManager.getPrisonerTime(p), ChronoUnit.SECONDS)
			jailManager.set("prisons." + jailManager.getPrisonName(p) + ".players." + p.uniqueId, seconds)
			jailManager.getPrisonersList().computeIfPresent(e.player.uniqueId) { _, _ -> null }
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onJoin(e: PlayerJoinEvent) {
		for (s in jailManager.getPrisonSections()) {
			if (jailManager.getFile()!!.isSet("prisons." + s + ".players." + e.player.uniqueId.toString())) {
				val region: String = jailManager.getFile()!!.getString("prisons.$s.region")!!
				val time: Int = jailManager.getFile()!!.getInt("prisons." + s + ".players." + e.player.uniqueId.toString())
				jailManager.getPrisonersList()[e.player.uniqueId] = Pair(jailManager.getRegions(s).getRegion(region)!!, Instant.now().plusSeconds(time.toLong()))
			}
		}
		if (wantedListManager.getPlayers().containsKey(e.player.uniqueId)){
			if (wantedListManager.getConfig()!!.isSet("logout-timer") && wantedListManager.getConfig()!!.isSet("logout-active")
				&& wantedListManager.getConfig()!!.getBoolean("logout-active")){
				if (System.currentTimeMillis() - e.player.lastPlayed > 60000L * wantedListManager.getConfig()!!.getInt("logout-timer")){
					wantedListManager.removePlayer(e.player.uniqueId)
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onDeath(e: PlayerDeathEvent) {
		if (wantedListManager.getPlayers().containsKey(e.entity.uniqueId)) wantedListManager.removePlayer(e.entity.uniqueId)
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onDamage(e: EntityDamageEvent) {
		if (jailManager.inPrison(e.entity.location)) e.isCancelled = true
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onCommand(e: PlayerCommandPreprocessEvent) {
		if (jailManager.isPrisoned(e.player)
			&& !whitelistManager.contains(
				e.message.lowercase(Locale.getDefault()).split(" ".toRegex()).dropLastWhile { it.isEmpty() }
					.toTypedArray()[0].substring(1))
		) {
			e.isCancelled = true
			e.player.sendMessage((messagesManager.messages["prison-permission"] as String))
		}
	}
}