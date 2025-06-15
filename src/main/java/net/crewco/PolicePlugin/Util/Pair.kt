package net.crewco.PolicePlugin.Util

class Pair<Left, Right>(val key: Left, val value: Right) {
	fun contains(o: Any): Boolean {
		return !(this.key != o && this.value != o)
	}

	companion object {
		fun <Left, Right> of(paramLeft: Left, paramRight: Right): Pair<Left, Right> {
			return Pair(paramLeft, paramRight)
		}
	}
}