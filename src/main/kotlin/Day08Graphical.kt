import util.PixelGameEngine
import java.awt.Color

class SIFVisualizer(image: String) : PixelGameEngine() {

    val layers = Day08(listOf(image)).layers.reversed()

    var layer = 0

    override fun onCreate() {
        construct(25, 6, 32, 32, "SIF")
    }

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        if (layer in layers.indices)
            for (y in 0 until screenHeight) {
                for (x in 0 until screenWidth) {
                    when (layers[layer][y * screenWidth + x]) {
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
    }

}

fun main() {
    SIFVisualizer(getInputAsString(8, 2019)).start()
}
