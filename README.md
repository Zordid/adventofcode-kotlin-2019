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