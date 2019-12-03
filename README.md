# Advent Of Code 2019
Solutions in Kotlin!

My solutions to the ingenious [Advent Of Code 2019](https://adventofcode.com/)
by Eric Wastl.

I am doing these challenges for the third year in a row now and I am totally 
addicted to them. If you are into programming, into logic, maybe also a little 
bit into competition, this one is for you as well!

### Day 1: The Tyranny of the Rocket Equation

As usual, we get into things with a little entry-level coding warm-up. The first
part is pure math. All that is interesting here to note, is that "rounding down" 
is the default in integer arithmetic, so no special treatment is needed. 
My favorite approach, as usual, is a functional way to state:

"Sum up all elements after applying this function":
```moduleMasses.sumBy { it.fuelForMass }```

Part 2 requires a little recursion to keep track of how much more fuel is needed
to carry the fuel you just calculated itself. Straight forward.

### Day 2: 1202 Program Alarm

Exceptionally early this year we hit the very first simple CPU emulation! It's a
ship computer running *Intcode* programs. Well essentially all CPUs run code that
is nothing more than numbers.

The trick here - and sadly something that cost me several minutes this morning - 
was to really make sure the indirect memory addressing works flawlessly.
Other than that, I spent some time this afternoon to refactor and build a ```ShipComputer```
class, ready for extensions in the upcoming puzzles!

### Day 3: Crossed Wires

Huray! Today's puzzle brought the first opportunity to go fancy with a graphical
visualization of the matter!

But let's start with the problem. It's a *"find common points of wires in 2D space
and find a point/coordinate that meets some criteria"*

So, following the given directional sequence of the wire layouts, one way to 
approach this is to first trace each and every point that is ever touched by a
wire and then do an intersection of the two sets of points to find only the common
points.

```val commonPoints = wirePoints[0].intersect(wirePoints[1])```

To further get the required single point, Kotlin again comes with easy to use 
functions on arbitrary data: ```minBy``` is your friend!

Furthermore, this puzzle led me to typealias a tuple (```Pair``` in Kotlin) to a 
```Point``` along with helpful extension properties and functions tailored for it like:

```
val Point.x: Int
    get() = first

val Point.y: Int
    get() = second

val Point.manhattanDistance: Int
    get() = x.absoluteValue + y.absoluteValue
```

For the graphical show-off I enjoyed using my new PixelGameEngine, inspired by the very
cool C++ [olcPixelGameEngine](https://github.com/OneLoneCoder/olcPixelGameEngine)!

You'll find a Day03Graphical.kt file that uses my new primitive engine as a support
to easily draw some lines (the wires) along with their respective common points. Check it out!