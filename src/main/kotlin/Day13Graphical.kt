import kotlinx.coroutines.ExperimentalCoroutinesApi
import util.PixelGameEngine
import java.awt.Color
import kotlin.random.Random

@ExperimentalCoroutinesApi
class Arcade : PixelGameEngine() {

    private val recorder = Day13().let {
        it.part2()
        it.recorder
    }

    private lateinit var gamePlay: Iterator<String>

    override fun onCreate() {
        recorder.takeWhile { !it.startsWith("joystick") }.forEach {
            if (it.startsWith("score")) {
                appName = it
            } else {
                val (x, y, t) = it.split(", ").map { it.toInt() }
                visualize(x, y, t)
            }
        }
        gamePlay = recorder.dropWhile { !it.startsWith("joystick") }.iterator()
    }

    val colors = listOf(Color.BLUE, Color.CYAN, Color.MAGENTA, Color.GREEN, Color.YELLOW)

    private fun visualize(x: Int, y: Int, t: Int) {
        val color = when (t) {
            0 -> Color.BLACK
            1 -> Color.RED
            2 -> colors[Random.nextInt(colors.size)]
            3 -> Color.PINK
            4 -> Color.WHITE
            else -> Color.BLACK
        }
        draw(x, y, color)
    }

    override fun onUpdate(elapsedTime: Long) {
        if (gamePlay.hasNext()) {
            val next = gamePlay.next()
            when {
                next.startsWith("joystick") -> {
                }

                next.startsWith("score") -> appName = next
                else -> {
                    val (x, y, t) = next.split(", ").map { it.toInt() }
                    visualize(x, y, t)
                    sleep(1000 / 40)
                }
            }
        }
    }

}

@ExperimentalCoroutinesApi
fun main() {
    with(Arcade()) {
        construct(44, 25, 16, 16)
        start()
    }
}