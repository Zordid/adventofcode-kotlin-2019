import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import util.PixelGameEngine
import util.completeAcyclicTraverse
import java.awt.Color

class Day15Graphical : PixelGameEngine() {

    val debugChannel = Channel<Boolean>(Channel.RENDEZVOUS)
    val day15 = Day15().apply {
        robot.debugChannel = debugChannel
    }

    lateinit var p1Job: Job

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        construct(50, 50, 8, 8, "AoC 2019 Day 15")
        clear(Color.DARK_GRAY)
        p1Job = GlobalScope.launch {
            delay(1500)
            day15.part1Async()
            day15.part2()
        }
    }

    var levels: List<Set<Point>>? = null
    var onLevel = 0

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        if (p1Job.isActive) {
            showMap()
            runBlocking { debugChannel.trySend(true).isSuccess }
        } else {
            if (levels == null) {
                showMap()
                appInfo = "target found"
                levels = day15.robot.knownGraph.completeAcyclicTraverse(day15.robot.targetPosition!!).toList()
                println("Now flooding with oxygen in ${levels?.size} levels!")
            }
            levels?.let {
                if (onLevel <= it.lastIndex)
                    showOxygen(it[onLevel])
                onLevel++
            }
        }
    }

    private fun showOxygen(oxygen: Set<Point>) {
        oxygen.forEach {
            val p = it - offset
            with(p) {
                draw(x, y, Color.BLUE)
            }
        }
    }

    var offset = -24 to -24

    private fun showMap() {
        val r = day15.robot
        val a = r.knownMap.keys.toList()
        val area = a.boundingBox()
        val midPointX = area.first.x + (area.second.x - area.first.x + 1) / 2
        val midPointY = area.first.y + (area.second.y - area.first.y + 1) / 2
        val offsetX = screenWidth / 2 - midPointX - 1
        val offsetY = screenHeight / 2 - midPointY - 1
        offset = -offsetX to -offsetY
        for (c in ((0 to 0) to (screenWidth to screenHeight)).allPoints()) {
            val p = c + offset
            val color = when {
                p == r.currentPosition -> Color.GREEN
                p == r.targetPosition -> Color.ORANGE
                p == origin -> Color.CYAN
                r.knownMap[p] == AreaType.FLOOR -> Color.BLACK
                r.knownMap[p] == AreaType.WALL -> Color.WHITE
                else -> Color.DARK_GRAY
            }
            color?.let { draw(c.x, c.y, it) }
        }
    }

}

fun main() {
    Day15Graphical().start()
}