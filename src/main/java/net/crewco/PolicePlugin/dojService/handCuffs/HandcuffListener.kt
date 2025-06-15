package net.crewco.PolicePlugin.dojService.handCuffs

import com.google.inject.Inject
import net.crewco.PolicePlugin.Startup
import net.crewco.PolicePlugin.Startup.Companion.handCuffManager
import net.crewco.PolicePlugin.Startup.Companion.jailManager
import net.crewco.PolicePlugin.Startup.Companion.messagesManager
import net.crewco.PolicePlugin.Startup.Companion.prisionGui
import net.crewco.PolicePlugin.Startup.Companion.searchGui
import net.crewco.PolicePlugin.Startup.Companion.utilsManager
import net.crewco.PolicePlugin.Startup.Companion.wantedListManager
import net.crewco.PolicePlugin.Startup.Companion.whitelistManager
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

class HandcuffListener @Inject constructor(private val plugin:Startup): Listener {
	private val clicks: MutableMap<Player, Int> = HashMap()
	private val release: MutableMap<Player, Int> = HashMap()

	init {
		Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
			for ((cuffer, cuffed) in handCuffManager.cuffed.entries) {
				val to = cuffed.location.clone().add(
					cuffed.location.direction.x * 2,
					0.0,
					cuffed.location.direction.z * 2
				)
				to.pitch = cuffer.location.pitch
				if (to.block.isPassable) {
					if (cuffer.location != to) {
						cuffer.teleport(to)
					}
				}
			}
		}, 0L, 1L)
	}
	fun faceLocation(entity: Entity, to: Location): Location? {
		if (entity.world !== to.world) {
			return null
		}
		val fromLocation = entity.location

		val xDiff = to.x - fromLocation.x
		val yDiff = to.y - fromLocation.y
		val zDiff = to.z - fromLocation.z

		val distanceXZ = sqrt(xDiff * xDiff + zDiff * zDiff)
		val distanceY = sqrt(distanceXZ * distanceXZ + yDiff * yDiff)

		var yaw = Math.toDegrees(acos(xDiff / distanceXZ))
		val pitch = Math.toDegrees(acos(yDiff / distanceY)) - 90.0
		if (zDiff < 0.0) {
			yaw += abs(180.0 - yaw) * 2.0
		}
		val loc = entity.location
		loc.yaw = (yaw - 90.0f).toFloat()
		loc.pitch = (pitch - 90.0f).toFloat()
		return loc
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onDamage(e: EntityDamageEvent) {
		if (e.entity is Player
			&& (handCuffManager.cuffed.containsKey(e.entity) || handCuffManager.cuffed.containsValue(e.entity))
		) {
			e.isCancelled = true
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onInteract(e: PlayerInteractEvent) {
		if (handCuffManager.cuffed.containsKey(e.player)) {
			e.isCancelled = true
		}
		if (handCuffManager.holdingCuff(e.player)) e.isCancelled = true
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onLeave(e: PlayerQuitEvent) {
		if (handCuffManager.cuffed.containsKey(e.player)) {
			if (jailManager.getPrisons().isEmpty()) return
			val lore: MutableList<String> = ArrayList()
			lore.add(utilsManager.color("&7&lOfficer: &f" + handCuffManager.getCuffer(e.player)!!.name))
			lore.add(utilsManager.color("&7&lArrested: &f" + e.player.name))
			lore.add(utilsManager.color("&7&lDate: &f" + (Date()).toString()))
			lore.add(utilsManager.color("&7&lCharges:"))
			lore.add("&b- &cDisconnected")
			val total: Int = plugin.maxtime * 60
			lore.add(
				utilsManager.color(
					"&7&lTime: &f" + (if (total / 60 != 0)
						((total / 60).toString() + "m" + (if (total % 60 > 0) ((total % 60).toString() + "s") else ""))
					else
						((total % 60).toString() + "s"))
				)
			)
			val slip: ItemStack = utilsManager.getItem(ItemStack(Material.PAPER), "&bJail File", lore)
			e.player.inventory.addItem(slip)
			handCuffManager.getCuffer(e.player)!!.inventory.addItem(slip)
			handCuffManager.uncuffPlayer(e.player)
			jailManager.imprisonPlayer(e.player, jailManager.getPrisons()[0], total)
		} else if (handCuffManager.cuffed.containsValue(e.player)) {
			handCuffManager.uncuffPlayer(handCuffManager.getCuffed(e.player)!!)
		} else if (wantedListManager.getPlayers().containsKey(e.player.uniqueId)) {
			if (wantedListManager.getConfig()!!.isSet("logout-timer") && wantedListManager.getConfig()!!.isSet("logout-active") && wantedListManager.getConfig()!!.getBoolean("logout-active")){
				Bukkit.getScheduler().runTaskLater(plugin, Runnable {
					if (e.player.isOnline) return@Runnable
					wantedListManager.removePlayer(e.player.uniqueId)
				}, (1200L * wantedListManager.config!!.getInt("logout-timer")))
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onCommand(e: PlayerCommandPreprocessEvent) {
		if (handCuffManager.cuffed.containsKey(e.player)
			&& !whitelistManager.contains(
				e.message.lowercase(Locale.getDefault()).split(" ".toRegex()).dropLastWhile { it.isEmpty() }
					.toTypedArray()[0].substring(1))
		) e.isCancelled =
			true
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onCommand(e: InventoryClickEvent) {
		if (handCuffManager.cuffed.containsKey(e.whoClicked)) {
			e.isCancelled = true
			e.whoClicked.closeInventory()
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onCommand(e: PlayerDropItemEvent) {
		if (handCuffManager.cuffed.containsKey(e.player)) e.isCancelled = true
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onInteract(e: PlayerInteractAtEntityEvent) {
		if (!(e.rightClicked is Player && e.hand == EquipmentSlot.HAND
					&& handCuffManager.holdingCuff(e.player))
		) return
		if (!e.player.hasPermission("prison.handcuffs.use")) {
			e.player.sendMessage((messagesManager.messages.get("invalid-permission") as String))
			return
		}
		if (jailManager.isPrisoned(e.rightClicked as Player)) {
			jailManager.prisonerToCell(e.rightClicked as Player)
			return
		}
		if (handCuffManager.cuffed.containsValue(e.player) && handCuffManager.cuffed.containsKey(e.rightClicked as Player)) {
			if (jailManager.inDropOff(e.player)) {
				if (!e.player.hasPermission("prison.arrest")) {
					e.player.sendMessage((messagesManager.messages["invalid-permission"] as String))
					return
				}
				prisionGui.create(e.player, e.rightClicked as Player, jailManager.getPrisonFromDropOff(e.player))
					.show(e.player, 0)
				return
			}
			if (e.player.isSneaking && e.player.hasPermission("prison.search.view")) {
				if (!e.player.hasPermission("prison.search.view")) {
					e.player.sendMessage((messagesManager.messages["invalid-permission"] as String))
					return
				}
				e.player.openInventory((e.rightClicked as Player).inventory)
				searchGui.create(e.player, (e.rightClicked as Player)).show(e.player, 0)
				return
			}
			if (!release.containsKey(e.player)) (object : BukkitRunnable() {
				var i: Int = 0

				override fun run() {
					if (this.i == 0) release[e.player] = 0
					if ((release[e.player] as Int) == 5 * this.i) {
						if (this.i == 2) {
							handCuffManager.uncuffPlayer(e.rightClicked as Player)
							release.remove(e.player)
							e.player.spigot().sendMessage(
								ChatMessageType.ACTION_BAR,
								*(ComponentBuilder(
									(messagesManager.messages["cuffer-release-success"] as String)
										.replace("%player%".toRegex(), e.rightClicked.name)
								)).create()
							)
							(e.rightClicked as Player).spigot().sendMessage(
								ChatMessageType.ACTION_BAR,
								*(ComponentBuilder(
									(messagesManager.messages["cuffing-release-success"] as String)
										.replace("%player%".toRegex(), e.player.name)
								))
									.create()
							)
							cancel()
						} else {
							e.player.spigot()
								.sendMessage(
									ChatMessageType.ACTION_BAR,
									*(ComponentBuilder(
										(messagesManager.messages["cuffer-release"] as String)
											.replace(
												"%seconds%".toRegex(), (StringBuilder((2 - this.i).toString()))
													.toString()
											)
											.replace("%player%".toRegex(), e.rightClicked.name)
									))
										.create()
								)
							(e.rightClicked as Player).spigot().sendMessage(
								ChatMessageType.ACTION_BAR,
								*(ComponentBuilder(
									(messagesManager.messages.get("cuffing-release") as String)
										.replace(
											"%seconds%".toRegex(), (StringBuilder((2 - this.i).toString()))
												.toString()
										)
										.replace("%player%".toRegex(), e.player.name)
								))
									.create()
							)
							i++
						}
					} else {
						e.player.spigot().sendMessage(
							ChatMessageType.ACTION_BAR,
							*(ComponentBuilder(messagesManager.messages["cuffer-release-fail"] as String))
								.create()
						)
						(e.rightClicked as Player).spigot().sendMessage(
							ChatMessageType.ACTION_BAR,
							*(ComponentBuilder(messagesManager.messages["cuffing-release-fail"] as String))
								.create()
						)
						release.remove(e.player)
						cancel()
					}
				}
			}).runTaskTimer(plugin, 0L, 20L)
			release.computeIfPresent(e.player) { _, v -> v + 1 }
			return
		}
		if (!handCuffManager.getCuffed().containsKey(e.rightClicked as Player)) {
			if (!clicks.containsKey(e.player)) (object : BukkitRunnable() {
				var i: Int = 0

				override fun run() {
					clicks.putIfAbsent(e.player, 0)
					if (clicks[e.player] == (5 * i)) {
						if (i == plugin.cufftimer) {
							handCuffManager.cuffPlayer(e.player, e.rightClicked as Player)
							clicks.remove(e.player)
							e.player.spigot()
								.sendMessage(
									ChatMessageType.ACTION_BAR,
									*(ComponentBuilder(
										(messagesManager.messages.get("cuffer-success") as String)
											.replace("%player%".toRegex(), e.rightClicked.name)
									))
										.create()
								)
							(e.rightClicked as Player).spigot().sendMessage(
								ChatMessageType.ACTION_BAR,
								*(ComponentBuilder(
									(messagesManager.messages.get("cuffing-success") as String)
										.replace("%player%".toRegex(), e.player.name)
								)).create()
							)
							cancel()
						} else {
							e.player.spigot()
								.sendMessage(
									ChatMessageType.ACTION_BAR,
									*(ComponentBuilder(
										(messagesManager.messages.get("cuffer-timer") as String)
											.replace(
												"%seconds%".toRegex(), (StringBuilder(
													java.lang.String
														.valueOf(plugin.cufftimer - this.i)
												))
													.toString()
											)
											.replace("%player%".toRegex(), e.rightClicked.name)
									))
										.create()
								)
							(e.rightClicked as Player).spigot()
								.sendMessage(
									ChatMessageType.ACTION_BAR,
									*(ComponentBuilder(
										(messagesManager.messages["cuffing-timer"] as String)
											.replace(
												"%seconds%".toRegex(), (StringBuilder(
													java.lang.String
														.valueOf(plugin.cufftimer - this.i)
												))
													.toString()
											)
											.replace("%player%".toRegex(), e.player.name)
									))
										.create()
								)
							i++
						}
					} else {
						e.player.spigot().sendMessage(
							ChatMessageType.ACTION_BAR,
							*(ComponentBuilder(messagesManager.messages["cuffer-fail"] as String)).create()
						)
						(e.rightClicked as Player).spigot().sendMessage(
							ChatMessageType.ACTION_BAR,
							*(ComponentBuilder(messagesManager.messages["cuffing-fail"] as String)).create()
						)
						clicks.remove(e.player)
						cancel()
					}
				}
			}).runTaskTimer(plugin, 0L, 20L)
			clicks.computeIfPresent(e.player) { _, v -> v + 1 }
			return
		}
	}
}