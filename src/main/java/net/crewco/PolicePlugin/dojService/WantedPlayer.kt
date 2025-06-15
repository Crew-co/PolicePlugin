package net.crewco.PolicePlugin.dojService

import java.util.*

class WantedPlayer(private var uuid: UUID, private var who: UUID, private var priority: Int, private var reason: String) {


	fun getUuid(): UUID {
		return uuid
	}

	fun setUuid(uuid: UUID?) {
		if (uuid != null) {
			this.uuid = uuid
		}
	}

	fun getPriority(): Int {
		return priority
	}

	fun setPriority(priority: Int?) {
		if (priority != null) {
			this.priority = priority
		}
	}

	fun getReason(): String {
		return reason
	}

	fun setReason(reason: String?) {
		if (reason != null) {
			this.reason = reason
		}
	}

	fun getWho(): UUID {
		return who
	}

	fun setWho(who: UUID?) {
		if (who != null) {
			this.who = who
		}
	}
}