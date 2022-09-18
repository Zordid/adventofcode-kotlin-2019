import util.PixelGameEngine
import java.awt.Color

class SIFVisualizer(image: String) : PixelGameEngine() {

    val layers = Day08(listOf(image)).layers.reversed()

    var width = 0
    var height = 0
    var layer = 0

    override fun onCreate() {
        appName = "SIF"
        width = screenWidth
        height = screenHeight
    }

    override fun onUpdate(elapsedTime: Long) {
        if (layer in layers.indices)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    when (layers[layer][y * width + x]) {
                        '0' -> draw(x, y, Color.BLACK)
                        '1' -> draw(x, y, Color.WHITE)
                    }
                }
            }
        layer++
        if (layer > layers.size + 50) {
            clear()
            layer = -50
        }
        sleep(100)
    }

}

fun main() {
    with(SIFVisualizer(getInputAsString(8, 2019))) {
        construct(25, 6, 32, 32)
        start()
    }
}
