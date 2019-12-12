import kotlin.math.absoluteValue

data class Vector3D(val x: Int, val y: Int, val z: Int) {
    override fun toString(): String {
        return "<x=$x, y=$y, z=$z>"
    }
}

fun stringToVector(s: String): Vector3D {
    return s.trimStart('<').trimEnd('>').split(", ")
        .map { it.substring(it.indexOf('=') + 1).toInt() }.let { (x, y, z) -> Vector3D(x, y, z) }
}

val Vector3D.manhattanDistance: Int
    get() = x.absoluteValue + y.absoluteValue + z.absoluteValue

operator fun Vector3D.plus(other: Vector3D) =
    Vector3D(this.x + other.x, this.y + other.y, this.z + other.z)

operator fun Vector3D.minus(other: Vector3D) =
    Vector3D(this.x - other.x, this.y - other.y, this.z - other.z)

val Vector3D.absoluteValue: Vector3D
    get() = Vector3D(x.absoluteValue, y.absoluteValue, z.absoluteValue)

class Day12(testData: List<String>? = null) : Day<Vector3D>(12, 2019, ::stringToVector, testData) {

    val constellation =
        input.map { pos -> pos to Vector3D(0, 0, 0) }

    var moonsInTime = constellation.toMutableList()

    var steps = 0

    fun print() {
        println("\nAfter $steps steps:")
        moonsInTime.forEach { (pos, vel) ->
            println("pos= $pos, vel= $vel ... energy: ${(pos to vel).energy()}")
        }
        println("Total energy: ${moonsInTime.sumBy { it.energy() }}")
    }

    fun step() {
        moonsInTime.indices.forEach { idx1 ->
            (idx1 + 1..moonsInTime.lastIndex).forEach { idx2 ->
                val m1 = moonsInTime[idx1]
                val m2 = moonsInTime[idx2]
                val diffVector = Vector3D(
                    m1.first.x.compareTo(m2.first.x),
                    m1.first.y.compareTo(m2.first.y),
                    m1.first.z.compareTo(m2.first.z)
                )
                moonsInTime[idx1] = m1.first to m1.second - diffVector
                moonsInTime[idx2] = m2.first to m2.second + diffVector
            }
        }
        moonsInTime.forEachIndexed { idx, m ->
            moonsInTime[idx] = m.first + m.second to m.second
        }
        steps++
    }

    fun Pair<Vector3D, Vector3D>.energy() = first.manhattanDistance * second.manhattanDistance

    fun lcm(a: Long, b: Long) = a * b / gcd(a, b)

    override fun part1(): Int {
        repeat(1000) {
            step()
        }
        return moonsInTime.sumBy { it.energy() }
    }

    override fun part2(): Any? {
        val cX = constellation.map { it.first.x }
        val sX = simulate(cX)
        val cY = constellation.map { it.first.y }
        val sY = simulate(cY)
        val cZ = constellation.map { it.first.z }
        val sZ = simulate(cZ)

        return lcm(listOf(sX.toLong(), sY.toLong(), sZ.toLong()))
    }

    private fun lcm(x: List<Long>): Long {
        if (x.size == 1)
            return x[0]
        return lcm(x[0], lcm(x.slice(1..x.lastIndex)))
    }
}

fun simulate(pInitial: List<Int>): Int {
    val vInitial = pInitial.map { 0 }
    val p = pInitial.toMutableList()
    val v = vInitial.toMutableList()

    //println("$p v= $v")
    var steps = 0
    do {
        steps++
        p.indices.forEach { a ->
            (a + 1..p.lastIndex).forEach { b ->
                val diff = p[a].compareTo(p[b])
                v[a] -= diff
                v[b] += diff
            }
        }
        p.indices.forEach {
            p[it] += v[it]
        }
        //println("$p v= $v")
    } while (v != vInitial)
    steps *= 2
    //println("Result: $steps")
    return steps
}

fun main() {
    Day12().run()
}