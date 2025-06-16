package net.crewco.PolicePlugin.vehiclesIntegration.listeners

import net.crewco.PolicePlugin.Startup.Companion.handCuffManager
import net.crewco.PolicePlugin.Startup.Companion.sysMsg
import nl.sbdeveloper.vehiclesplus.api.VehiclesPlusAPI
import nl.sbdeveloper.vehiclesplus.api.events.impl.VehicleClickEvent
import nl.sbdeveloper.vehiclesplus.api.nbt.NBTDataType
import nl.sbdeveloper.vehiclesplus.api.vehicles.impl.DrivableVehicle
import nl.sbdeveloper.vehiclesplus.api.vehicles.impl.SpawnedVehicle
import nl.sbdeveloper.vehiclesplus.api.vehicles.parts.Part
import nl.sbdeveloper.vehiclesplus.api.vehicles.parts.impl.Wheel
import nl.sbdeveloper.vehiclesplus.api.vehicles.parts.impl.seat.Seat
import nl.sbdeveloper.vehiclesplus.api.vehicles.parts.impl.skin.Skin
import org.bukkit.Bukkit
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import java.util.*


class clickListener: Listener {
	@EventHandler
	fun onVehicleClick(event: PlayerInteractAtEntityEvent) {
		val api = VehiclesPlusAPI.getVehicles()
		val player = event.player
		val clicked = event.rightClicked
		if (clicked.type == EntityType.ARMOR_STAND) {
			player.sendMessage("${sysMsg}DBG-TY: ${clicked.type}")
			event.isCancelled = true
			player.sendMessage("${sysMsg}DBG-TY: ${clicked.type}")
			player.sendMessage("${sysMsg}DBG-NM: ${clicked.name}")
			val armorStand: ArmorStand = clicked as ArmorStand
			if (armorStand.customName != null && armorStand.name.contains("VP_PART")) {
					when (clicked.name) {
						"VP_PART" -> {
							try {
								//player.sendMessage("${sysMsg}DBG-TYP:${armorStand.getMetadata(NBTDataType.V_PART_DATA.name)[0].value()?.javaClass?.name}")
								val part = armorStand.getMetadata(NBTDataType.V_PART_DATA.name)[0].value() as Seat
								val spawnedVehicle: Optional<SpawnedVehicle>? = part.owningVehicle
								if (handCuffManager.isCuffed(handCuffManager.getCuffed(player))){
									part.enter(handCuffManager.getCuffed(player))
								}

								//player.sendMessage("${sysMsg}Found-VH:${spawnedVehicle?.get()?.holder?.name()}")

							} catch (e: ClassCastException) {
								player.sendMessage("${sysMsg}Must Click on a Door/Seat")
								//Bukkit.getLogger().info("${sysMsg}Err:${e}")
								//val partRaw = armorStand.getMetadata(NBTDataType.V_PART_DATA.name)[0].value()
								//player.sendMessage("${sysMsg}Part loader: ${partRaw!!::class.java.classLoader}")
								//player.sendMessage("${sysMsg}Seat loader: ${Seat::class.java.classLoader}")
							}
						}
					}

					//val spawnedVehicle: Optional<SpawnedVehicle>? = part.owningVehicle
					//player.sendMessage("${sysMsg}Found-VH:${spawnedVehicle?.get()?.holder?.name()}")
				}
			}
		}
	}