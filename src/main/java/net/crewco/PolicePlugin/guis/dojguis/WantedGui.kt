package net.crewco.PolicePlugin.guis.dojguis

import net.crewco.PolicePlugin.Startup
import net.crewco.PolicePlugin.Startup.Companion.messagesManager
import net.crewco.PolicePlugin.Startup.Companion.utilsManager
import net.crewco.PolicePlugin.Startup.Companion.wantedListManager
import net.crewco.PolicePlugin.Util.Pair
import net.crewco.PolicePlugin.dojService.WantedPlayer
import net.crewco.PolicePlugin.guis.listener.Gui
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import javax.inject.Inject

class WantedGui  @Inject constructor(private val plugin:Startup){
	fun view(player: Player): Gui {
		val gui = Gui(plugin)
		val items: Array<ItemStack> = wantedListManager.getHeads()
		val template = gui.createTemplate("&8WantedList View", 54).fillRow(0, Material.GRAY_STAINED_GLASS_PANE, " ")
			.fillRow(5, Material.GRAY_STAINED_GLASS_PANE, " ").fillColumn(0, Material.GRAY_STAINED_GLASS_PANE, " ")
			.fillColumn(8, Material.GRAY_STAINED_GLASS_PANE, " ")
			.setItem(5, 1, Material.RED_STAINED_GLASS_PANE, "&cBack")
			.setItem(5, 3, Material.LIME_CONCRETE, "&aAdd Player").setItem(5, 5, Material.RED_CONCRETE, "&cClose")
			.setItem(5, 7, Material.LIME_STAINED_GLASS_PANE, "&aNext").noClick().onClick { e ->
				if (e.slot == 52) gui.nextPage(player)
				else if (e.slot == 50) gui.close(player)
				else if (e.slot == 46) gui.prevPage(player)
				else if (e.slot == 48) {
					if (!player.hasPermission("prison.wl.add")) {
						player.sendMessage(messagesManager.messages.get("invalid-permission")!!)
						gui.close(player)
						return@onClick
					}
					add(player).show(player, 0)
				} else if (e.currentItem != null
					&& e.currentItem!!.type == Material.PLAYER_HEAD
				) {
					if (!player.hasPermission("prison.wl.update")) {
						player.sendMessage(messagesManager.messages.get("invalid-permission")!!)
						gui.close(player)
						return@onClick
					}
					update(
						player,
						wantedListManager.getPlayers().get(
							(e.currentItem!!.itemMeta as SkullMeta).owningPlayer!!.uniqueId
						)!!
					)
						.show(player, 0)
				}
			}
		gui.create(template,28,*items)
		return gui
	}

	fun add(player: Player): Gui {
		val gui = Gui(plugin)

		val items = Bukkit.getOnlinePlayers()
			.filter { !wantedListManager.getPlayers().containsKey(it.uniqueId) }
			.map { utilsManager.getSkull(it) }

		val heads = items.toTypedArray()

		gui.create(
			gui.createTemplate("&8Add a Player - Player Selection", 54)
				.fillRow(0, Material.GRAY_STAINED_GLASS_PANE, " ")
				.fillRow(5, Material.GRAY_STAINED_GLASS_PANE, " ")
				.fillColumn(0, Material.GRAY_STAINED_GLASS_PANE, " ")
				.fillColumn(8, Material.GRAY_STAINED_GLASS_PANE, " ")
				.setItem(5, 1, Material.RED_STAINED_GLASS_PANE, "&cBack")
				.setItem(5, 7, Material.LIME_STAINED_GLASS_PANE, "&aNext")
				.noClick()
				.onClick { e ->
					when (e.slot) {
						52 -> gui.nextPage(player)
						46 -> gui.prevPage(player)
						else -> {
							val item = e.currentItem
							if (item?.type == Material.PLAYER_HEAD) {
								val meta = item.itemMeta as? SkullMeta
								val target = meta?.owningPlayer
								if (target != null) {
									priority(player, target, 0).show(player, 0)
								}
							}
						}
					}
				}, 28, *heads
		)

		gui.getPage(0).onClick { e ->
			when (e.slot) {
				52 -> gui.nextPage(player)
				46 -> view(player).show(player, 0)
				else -> {
					val item = e.currentItem
					if (item?.type == Material.PLAYER_HEAD) {
						val meta = item.itemMeta as? SkullMeta
						val target = meta?.owningPlayer
						if (target != null) {
							priority(player, target, 0).show(player, 0)
						}
					}
				}
			}
		}

		return gui
	}

	fun priority(player: Player, target: OfflinePlayer, returnTo: Byte): Gui {
		val gui = Gui(plugin)
		val prior = intArrayOf(1)
		gui.create("&8Player Priority Level", 27).fill(Material.GRAY_STAINED_GLASS_PANE, " ")
			.setItem(1, 2, Material.GREEN_CONCRETE, "&2Level 1 - Lowest")
			.setItem(1, 3, Material.LIME_CONCRETE, "&aLevel 2 - Low")
			.setItem(1, 4, Material.YELLOW_CONCRETE, "&eLevel 3 - Moderate")
			.setItem(1, 5, Material.ORANGE_CONCRETE, "&6Level 4 - High")
			.setItem(1, 6, Material.RED_CONCRETE, "&4Level 5 - Highest")
			.setItem(2, 1, Material.RED_STAINED_GLASS_PANE, "&cBack")
			.setItem(2, 7, Material.LIME_STAINED_GLASS_PANE, "&aConfirm").addGlow(1, 1 + prior[0]).noClick()
			.onClick { e ->
				if (e.slot == 25) {
					wantedListManager.addUpdate(
						player.uniqueId,
						WantedPlayer(target.uniqueId, player.uniqueId, prior[0], null.toString())
					)
					player.sendMessage(
						utilsManager.replaceAll(
							messagesManager.messages.get("chat-reason")!!,
							Pair.of("%words%", "" + plugin.getConfig().getInt("settings.word-limit")),
							Pair.of(
								"%characters%",
								"" + plugin.config.getInt("settings.character-limit")
							)
						)
					)
					gui.close(player)
				} else if (e.slot == 19) {
					if (returnTo == 0.toByte()) {
						add(player).show(player, 0)
					} else update(player, wantedListManager.getPlayers().get(target.uniqueId)!!).show(player, 0)
				} else if (e.slot in 11..15) {
					gui.getPage(0).removeGlow(1, 1 + prior[0])
					prior[0] = e.slot - 10
					gui.getPage(0).addGlow(1, 1 + prior[0])
				}
			}
		return gui
	}

	fun confirm(user: Player, wp: WantedPlayer, confirm: Byte): Gui {
		val gui = Gui(plugin)
		val skull = ItemStack(Material.PLAYER_HEAD, 1)
		val skullMeta = skull.itemMeta as SkullMeta
		val p = Bukkit.getOfflinePlayer(wp.getUuid())
		val whom = Bukkit.getOfflinePlayer(wp.getWho())
		skullMeta.setDisplayName(
			utilsManager.color(
				(if (p.player == null) p.name else p.player!!.displayName)?.let {
					wantedListManager.getTemplate()?.get(0)!!.replace(
						"%player%",
						it
					)
				}
			).toString()
		)
		skullMeta.setOwningPlayer(p)
		skullMeta.lore = utilsManager.colorList(
			wantedListManager.getTemplate()!!.subList(1, wantedListManager.getTemplate()!!.size),
			Pair.of("%player%", p.name!!), Pair.of("%who%", whom.name!!),
			Pair.of("%priority%", "" + wp.getPriority()), Pair.of("%reason%", wp.getReason())
		)
		skull.setItemMeta(skullMeta)
		gui.create(if (confirm.toInt() == 0) "&8Add a Player Confirm" else "&8Remove a Player Confirm", 27)
			.fill(Material.GRAY_STAINED_GLASS_PANE, " ").setItem(1, 4, skull)
			.setItem(2, 1, Material.RED_CONCRETE, "&cClose").setItem(2, 7, Material.LIME_CONCRETE, "&aConfirm")
			.noClick().onClick { e ->
				if (e.slot == 25) {
					if (confirm.toInt() == 0) {
						if (wantedListManager.getPlayers().containsKey(wp.getUuid())) wantedListManager.updatePlayer(
							wp.getUuid(), user.uniqueId, wp.getPriority(),
							wp.getReason()
						)
						else wantedListManager.addPlayer(
							wp.getUuid(), user.uniqueId, wp.getPriority(),
							wp.getReason()
						)
						wantedListManager.removeUpdate(user.uniqueId)
						gui.close(user)
					} else {
						wantedListManager.guiRemovePlayer(user, wp.getUuid())
						gui.close(user)
					}
				} else if (e.slot == 19) {
					gui.close(user)
				}
			}
		return gui
	}

	fun update(player: Player, wp: WantedPlayer): Gui {
		val gui = Gui(plugin)
		gui.create("&8Update/Remove a Player", 27).fill(Material.GRAY_STAINED_GLASS_PANE, " ")
			.setItem(1, 3, Material.LIME_CONCRETE, "&aUpdate Player")
			.setItem(1, 5, Material.RED_CONCRETE, "&cRemove Player").noClick().onClick { e ->
				if (e.slot == 12) {
					priority(player, Bukkit.getOfflinePlayer(wp.getUuid()), 1.toByte()).show(player, 0)
				} else if (e.slot == 14) {
					if (!player.hasPermission("prison.wl.remove")) {
						player.sendMessage(messagesManager.messages.get("invalid-permission")!!)
						gui.close(player)
						return@onClick
					}
					confirm(player, wp, 1.toByte()).show(player, 0)
				}
			}
		return gui
	}

}