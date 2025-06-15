package net.crewco.PolicePlugin.docService


import com.google.inject.Inject
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.managers.RegionManager
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import com.sk89q.worldguard.protection.regions.RegionContainer
import net.crewco.PolicePlugin.Startup
import net.crewco.PolicePlugin.Util.Pair
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class Jail @Inject constructor(private val plugin: Startup) {
	private var prisonsFile: File? = null

	private var config: FileConfiguration? = null

	private var container: RegionContainer = WorldGuard.getInstance().platform.regionContainer

	private var jails: MutableMap<ProtectedRegion, String> = HashMap<ProtectedRegion, String>()

	private var cells: MutableMap<String, List<ProtectedRegion>> = HashMap<String, List<ProtectedRegion>>()

	private var prisoners: MutableMap<UUID, Pair<ProtectedRegion, Instant>> = HashMap<UUID, Pair<ProtectedRegion, Instant>>()

	private var dropoff: MutableMap<ProtectedRegion, ProtectedRegion> = HashMap<ProtectedRegion, ProtectedRegion>()

	private var release: MutableMap<ProtectedRegion, ProtectedRegion> = HashMap<ProtectedRegion, ProtectedRegion>()

	fun getFile(): FileConfiguration? {
		return config
	}

	fun reload() {
		config = YamlConfiguration.loadConfiguration(prisonsFile!!)
	}

	private fun save() {
		try {
			prisonsFile?.let { config?.save(it) }
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	fun set(path: String, `object`: Any?) {
		config?.set(path, `object`)
		save()
	}

	fun getRegions(prison: String): RegionManager {
		if (config!!.isSet("prisons.$prison.world") &&
			config!!.getString("prisons.$prison.world")?.let { Bukkit.getWorld(it) } != null
		) return container
			.get(BukkitAdapter.adapt(config!!.getString("prisons.$prison.world")?.let { Bukkit.getWorld(it) }))!!
		return container.get(BukkitAdapter.adapt(Bukkit.getWorlds()[0]))!!
	}

	fun loadFile() {
		container = WorldGuard.getInstance().platform.regionContainer
		jails = HashMap<ProtectedRegion, String>()
		cells = HashMap<String, List<ProtectedRegion>>()
		dropoff = HashMap<ProtectedRegion, ProtectedRegion>()
		release = HashMap<ProtectedRegion, ProtectedRegion>()
		prisonsFile = File(plugin.dataFolder, "prisons.yml")
		if (!prisonsFile!!.exists()){ plugin.saveResource("prisons.yml", false)}
		config = YamlConfiguration.loadConfiguration(prisonsFile!!) as FileConfiguration
		try {
			config!!.save(prisonsFile!!)
		} catch (e: IOException) {
			e.printStackTrace()
		}
		for (s in config!!.getConfigurationSection("prisons")?.getKeys(false)!!) {
			if (config!!.isSet("prisons.$s.region") && config!!.isSet("prisons.$s.drop-off") &&
				config!!.isSet("prisons.$s.release")
			) {
				val region: String = config!!.getString("prisons.$s.region").toString()
				val drop: String = config!!.getString("prisons.$s.drop-off").toString()
				val rel: String = config!!.getString("prisons.$s.release").toString()
				if (getRegions(s).hasRegion(region) && getRegions(s).hasRegion(drop) && getRegions(s).hasRegion(rel)) {
					jails[getRegions(s).getRegion(region)!!] = s
					dropoff[getRegions(s).getRegion(drop)!!] = getRegions(s).getRegion(region)!!
					release[getRegions(s).getRegion(region)!!] = getRegions(s).getRegion(rel)!!
				} else {
					continue
				}
			} else {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED.toString() + "Failed to load the regions for " + s)
			}
			if (config!!.isSet("prisons.$s.cells")) {
				val temp: MutableList<ProtectedRegion> = ArrayList()
				for (cell in config!!.getStringList("prisons.$s.cells")) {
					if (getRegions(s).hasRegion(cell)) {
						temp.add(getRegions(s).getRegion(cell)!!)
						continue
					}
					Bukkit.getConsoleSender()
						.sendMessage(ChatColor.RED.toString() + "Failed to load the cell " + cell + " for " + s)
				}
				cells[s] = temp
			} else {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED.toString() + "Failed to load the cells " + s)
			}
			if (config!!.isSet("prisons.$s.players")) {
				if (prisoners.isNotEmpty()){ savePrisonerData()}
				prisoners = HashMap<UUID, Pair<ProtectedRegion, Instant>>()
				for (uuid in config!!.getConfigurationSection("prisons.$s.players")!!.getKeys(false)) {
					val player = UUID.fromString(uuid)
					val region: String = config!!.getString("prisons.$s.region")!!
					val time: Int = config!!.getInt("prisons.$s.players.$uuid")
					prisoners[player] = Pair(getRegions(s).getRegion(region)!!, Instant.now().plusSeconds(time.toLong()))
				}
			}
		}
	}

	fun savePrisonerData() {
		for (p in getPrisoners()) {
			val seconds = Instant.now().until(getPrisonerTime(p!!), ChronoUnit.SECONDS)
			set("prisons." + getPrisonName(p) + ".players." + p.uniqueId, seconds)
		}
	}


	fun getPrisons(): List<ProtectedRegion> {
		return ArrayList(jails.keys)
	}

	fun getPrisonSections(): List<String> {
		return ArrayList(jails.values)
	}

	fun getPrisoners(): List<Player?> {
		val players: MutableList<Player?> = ArrayList()
		for (uuid in prisoners.keys) {
			if (Bukkit.getOfflinePlayer(uuid).isOnline) players.add(Bukkit.getPlayer(uuid))
		}
		return players
	}

	fun getNextAvaliable(prison: String?): ProtectedRegion? {
		var min = -1
		var next: ProtectedRegion? = null
		for (region in cells[prison]!!) {
			var count = 0
			for (p in Bukkit.getOnlinePlayers()) {
				if (region.contains(
						p.location.blockX, p.location.blockY,
						p.location.blockZ
					)
				) count++
			}
			if (min == -1) {
				min = count
				next = region
				continue
			}
			if (count < min) {
				next = region
				min = count
			}
		}
		return next
	}

	fun inDropOff(p: Player): Boolean {
		return getPrisonFromDropOff(p) != null
	}

	fun getPrisonFromDropOff(p: Player): ProtectedRegion? {
		for (region in dropoff.keys) {
			if (region.contains(
					p.location.blockX, p.location.blockY,
					p.location.blockZ
				)
			) return dropoff[region]
		}
		return null
	}

	fun getRelease(p: Player): ProtectedRegion {
		return release[getPrisonerPrison(p)]!!
	}

	fun getPrisonName(p: Player): String {
		return jails[getPrisonerPrison(p)]!!
	}

	fun isPrisoned(p: Player): Boolean {
		return prisoners.containsKey(p.uniqueId)
	}

	fun getPrisonerPrison(p: Player): ProtectedRegion {
		return getPrisonerInfo(p).key
	}

	fun getPrisonerTime(p: Player): Instant {
		return getPrisonerInfo(p).value
	}

	private fun getPrisonerInfo(p: Player): Pair<ProtectedRegion, Instant> {
		return getPrisoner(p)!!.value
	}

	fun getPrisonersList(): MutableMap<UUID, Pair<ProtectedRegion, Instant>> {
		return prisoners
	}

	fun prisonerToCell(p: Player) {
		val region: ProtectedRegion = getNextAvaliable(getPrisonName(p))!!
		val x = region.minimumPoint.x + (region.maximumPoint.blockX - region.minimumPoint.blockX) / 2
		val z = region.minimumPoint.z + (region.maximumPoint.blockZ - region.minimumPoint.blockZ) / 2
		val y: Int = highestBlock(region, x, z, p.world)
		p.teleport(Location(p.world, x.toDouble(), y.toDouble(), z.toDouble()))
	}

	private fun getPrisoner(p: Player): Map.Entry<UUID, Pair<ProtectedRegion, Instant>>? {
		for (e in prisoners.entries) {
			if (e.key == p.uniqueId) return e
		}
		return null
	}

	fun inPrison(l: Location): Boolean {
		for (region in jails.keys) {
			if (region.contains(l.blockX, l.blockY, l.blockZ)) return true
		}
		return false
	}

	fun imprisonPlayer(p: Player, prison: ProtectedRegion, time: Int) {
		prisoners[p.uniqueId] = Pair(prison, Instant.now().plusSeconds(time.toLong()))
		set("prisons." + getPrisonName(p) + ".players." + p.uniqueId.toString(), time)
		prisonerToCell(p)
	}

	fun canRelease(p: Player): Boolean {
		return getPrisonerTime(p).isBefore(Instant.now())
	}

	private fun highestBlock(region: ProtectedRegion, x: Int, z: Int, world: World): Int {
		val low = region.minimumPoint.blockY
		val high = region.maximumPoint.blockY
		for (i in low..<high) {
			if (world.getBlockAt(x, i, z).type == Material.AIR &&
				region.contains(x, i, z)
			) return i
		}
		return world.getHighestBlockYAt(x, z)
	}

	fun releasePlayer(p: Player) {
		set("prisons." + getPrisonName(p) + ".players." + p.uniqueId.toString(), null)
		val x: Int = getRelease(p).minimumPoint.x + (getRelease(p).maximumPoint
			.blockX - getRelease(p).minimumPoint.blockX) / 2
		val z: Int = getRelease(p).minimumPoint.z + (getRelease(p).maximumPoint
			.blockZ - getRelease(p).minimumPoint.blockZ) / 2
		val y: Int = highestBlock(getRelease(p), x, z, p.world)
		p.teleport(Location(p.world, x.toDouble(), y.toDouble(), z.toDouble()))
		prisoners.remove(p.uniqueId)
	}


}