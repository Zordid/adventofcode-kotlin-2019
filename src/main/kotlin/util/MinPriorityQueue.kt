package util

interface MinPriorityQueue<T> {

    fun isEmpty(): Boolean
    fun insert(element: T, priority: Int)
    fun remove(element: T)
    fun extractMin(): T
    fun decreasePriority(element: T, priority: Int)

}

class MinPriorityQueueImpl<T> : MinPriorityQueue<T> {
    private val elementToPrio = mutableMapOf<T, Int>()
    private val prioToElement = mutableMapOf<Int, MutableSet<T>>()
    private val priorities = sortedSetOf<Int>()

    override fun isEmpty() = elementToPrio.isEmpty()

    fun contains(element: T) = elementToPrio.containsKey(element)

    override fun insert(element: T, priority: Int) {
        if (element in elementToPrio)
            remove(element)
        elementToPrio[element] = priority
        prioToElement.getOrPut(priority) { mutableSetOf() }.add(element)
        priorities.add(priority)
    }

    override fun remove(element: T) {
        val priority = getPriorityOf(element)
        prioToElement[priority]!!.remove(element)
        if (prioToElement[priority]!!.isEmpty()) {
            prioToElement.remove(priority)
            priorities.remove(priority)
        }
        elementToPrio.remove(element)
    }

    private fun getPriorityOf(element: T) = elementToPrio[element]

    override fun extractMin(): T {
        if (elementToPrio.isEmpty())
            throw NoSuchElementException()
        val result = prioToElement[priorities.first()]!!.first()
        remove(result)
        return result
    }

    override fun decreasePriority(element: T, priority: Int) {
        remove(element)
        insert(element, priority)
    }

}

class PriorityQueue<T>(size: Int, val comparator: Comparator<T>? = null) : Collection<T> {
    public override var size: Int = 0
        private set
    private var arr: Array<T?> = Array<Comparable<T>?>(size, { null }) as Array<T?>

    public fun add(element: T) {
        if (size + 1 == arr.size) {
            resize()
        }
        arr[++size] = element
        swim(size)
    }

    public fun peek(): T {
        if (size == 0) throw NoSuchElementException()
        return arr[1]!!
    }

    public fun poll(): T {
        if (size == 0) throw NoSuchElementException()
        val res = peek()
        arr.exch(1, size--)
        sink(1)
        arr[size + 1] = null
        if ((size > 0) && (size == (arr.size - 1) / 4)) {
            resize()
        }
        return res
    }

    private fun swim(n: Int) {
        Companion.swim(arr, n, comparator)
    }

    private fun sink(n: Int) {
        Companion.sink(arr, n, size, comparator)
    }

    private fun resize() {
        val old = arr
        arr = Array<Comparable<T>?>(size * 2, { null }) as Array<T?>
        System.arraycopy(old, 0, arr, 0, size + 1)
    }

    public override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun contains(element: T): Boolean {
        for (obj in this) {
            if (obj == element) return true
        }
        return false
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        for (element in elements) {
            if (!contains(element)) return false
        }
        return true
    }

    override fun iterator(): Iterator<T> {
        return arr.copyOfRange(1, size + 1).map { it!! }.iterator()
    }

    companion object {
        private fun <T> greater(arr: Array<T?>, i: Int, j: Int, comparator: Comparator<T>? = null): Boolean {
            if (comparator != null) {
                return comparator.compare(arr[i], arr[j]) > 0
            } else {
                val left = arr[i]!! as Comparable<T>
                return left > arr[j]!!
            }
        }

        public fun <T> sink(arr: Array<T?>, a: Int, size: Int, comparator: Comparator<T>? = null) {
            var k = a
            while (2 * k <= size) {
                var j = 2 * k
                if (j < size && greater(arr, j, j + 1, comparator)) j++
                if (!greater(arr, k, j, comparator)) break
                arr.exch(k, j)
                k = j
            }
        }

        public fun <T> swim(arr: Array<T?>, size: Int, comparator: Comparator<T>? = null) {
            var n = size
            while (n > 1 && greater(arr, n / 2, n, comparator)) {
                arr.exch(n, n / 2)
                n /= 2
            }
        }
    }
}

fun <T> Array<T>.exch(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}