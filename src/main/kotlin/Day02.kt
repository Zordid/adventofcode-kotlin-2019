import kotlin.system.exitProcess

class Day02 : Day<String>(2, 2019, ::asStrings) {


    override fun part1(): Any? {
        val program = input[0].split(",").map { it.toInt() }.toMutableList()
        println(program)
        program[1] = 12
        program[2] = 2

        return run(program)
    }

    override fun part2(): Any? {
        val program = input[0].split(",").map { it.toInt() }

        for (noun in 0..99) {
            for (verb in 0..99) {
                val p = program.toMutableList()
                p[1] = noun
                p[2] = verb
                if (run(p) == 19690720) {
                    println("match: noun=$noun, verb=$verb")
                    return 100 * noun + verb
                }
            }
        }
        return -1
    }

    fun run(program: MutableList<Int>): Int {

        fun add(pos: Int) {
            val a = program[program[pos]]
            val b = program[program[pos + 1]]
            val dest = program[pos + 2]
            val result = a + b
            //println("ADD $a + $b = $result written in $dest")
            program[dest] = result
        }

        fun mult(pos: Int) {
            val a = program[program[pos]]
            val b = program[program[pos + 1]]
            val dest = program[pos + 2]
            val result = a * b
            //println("MULT $a * $b = $result written in $dest")
            program[dest] = result
        }

        var ip = 0
        var opcode = program[ip]

        while (true) {
            //println("processing $opcode @ $ip")
            ip += when (opcode) {
                1 -> {
                    add(ip + 1)
                    4
                }
                2 -> {
                    mult(ip + 1)
                    4
                }
                99 -> {
                    //println("Exit")
                    return program[0]
                }
                else -> error("WOP")
            }
            opcode = program[ip]
        }
    }

}

fun main() {
//    val program = mutableListOf(1,9,10,3,2,3,11,0,99,30,40,50)
//    println(program)
//    println(Day02().run(program))
//    println(program)
//    exitProcess(1)
    with(Day02()) {
        println(part1())
        println(part2())
    }
}