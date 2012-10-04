package kotlin


//
// NOTE THIS FILE IS AUTO-GENERATED by the GenerateStandardLib.kt
// See: https://github.com/JetBrains/kotlin/tree/master/libraries/stdlib
//
// Generated from input file: src/kotlin/Iterables.kt
//


import java.util.*

/**
 * Returns *true* if all elements match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt all
 */
public inline fun CharArray.all(predicate: (Char) -> Boolean) : Boolean {
    for (element in this) if (!predicate(element)) return false
    return true
}

/**
 * Returns *true* if any elements match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt any
 */
public inline fun CharArray.any(predicate: (Char) -> Boolean) : Boolean {
    for (element in this) if (predicate(element)) return true
    return false
}

/**
 * Appends the string from all the elements separated using the *separator* and using the given *prefix* and *postfix* if supplied
 *
 * If a collection could be huge you can specify a non-negative value of *limit* which will only show a subset of the collection then it will
 * a special *truncated* separator (which defaults to "..."
 *
 * @includeFunctionBody ../../test/CollectionTest.kt appendString
 */
public inline fun CharArray.appendString(buffer: Appendable, separator: String = ", ", prefix: String = "", postfix: String = "", limit: Int = -1, truncated: String = "..."): Unit {
    buffer.append(prefix)
    var count = 0
    for (element in this) {
        if (++count > 1) buffer.append(separator)
        if (limit < 0 || count <= limit) {
            val text = if (element == null) "null" else element.toString()
            buffer.append(text)
        } else break
    }
    if (limit >= 0 && count > limit) buffer.append(truncated)
    buffer.append(postfix)
}

/**
 * Returns the number of elements which match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt count
 */
public inline fun CharArray.count(predicate: (Char) -> Boolean) : Int {
    var count = 0
    for (element in this) if (predicate(element)) count++
    return count
}

/**
 * Returns the first element which matches the given *predicate* or *null* if none matched
 *
 * @includeFunctionBody ../../test/CollectionTest.kt find
 */
public inline fun CharArray.find(predicate: (Char) -> Boolean) : Char? {
    for (element in this) if (predicate(element)) return element
    return null
}

/**
 * Filters all elements which match the given predicate into the given list
 *
 * @includeFunctionBody ../../test/CollectionTest.kt filterIntoLinkedList
 */
public inline fun <C: MutableCollection<Char>> CharArray.filterTo(result: C, predicate: (Char) -> Boolean) : C {
    for (element in this) if (predicate(element)) result.add(element)
    return result
}

/**
 * Partitions this collection into a pair of collection
 *
 * @includeFunctionBody ../../test/CollectionTest.kt partition
 */
public inline fun CharArray.partition(predicate: (Char) -> Boolean) : Pair<List<Char>, List<Char>> {
    val first = ArrayList<Char>()
    val second = ArrayList<Char>()
    for (element in this) {
        if (predicate(element)) {
            first.add(element)
        } else {
            second.add(element)
        }
    }
    return Pair(first, second)
}

/**
 * Returns a list containing all elements which do not match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt filterNotIntoLinkedList
 */
public inline fun <C: MutableCollection<Char>> CharArray.filterNotTo(result: C, predicate: (Char) -> Boolean) : C {
    for (element in this) if (!predicate(element)) result.add(element)
    return result
}

/**
 * Filters all non-*null* elements into the given list
 *
 * @includeFunctionBody ../../test/CollectionTest.kt filterNotNullIntoLinkedList
 */
public inline fun <C: MutableCollection<Char>> CharArray?.filterNotNullTo(result: C) : C {
    if (this != null) {
        for (element in this) if (element != null) result.add(element)
    }
    return result
}

/**
 * Returns the result of transforming each element to one or more values which are concatenated together into a single list
 *
 * @includeFunctionBody ../../test/CollectionTest.kt flatMap
 */
public inline fun <R> CharArray.flatMapTo(result: MutableCollection<R>, transform: (Char) -> Collection<R>) : Collection<R> {
    for (element in this) {
        val list = transform(element)
        if (list != null) {
            for (r in list) result.add(r)
        }
    }
    return result
}

/**
 * Performs the given *operation* on each element
 *
 * @includeFunctionBody ../../test/CollectionTest.kt forEach
 */
public inline fun CharArray.forEach(operation: (Char) -> Unit) : Unit = for (element in this) operation(element)

/**
 * Folds all elements from from left to right with the *initial* value to perform the operation on sequential pairs of elements
 *
 * @includeFunctionBody ../../test/CollectionTest.kt fold
 */
public inline fun <R> CharArray.fold(initial: R, operation: (R, Char) -> R): R {
    var answer = initial
    for (element in this) answer = operation(answer, element)
    return answer
}

/**
 * Folds all elements from right to left with the *initial* value to perform the operation on sequential pairs of elements
 *
 * @includeFunctionBody ../../test/CollectionTest.kt foldRight
 */
public inline fun <R> CharArray.foldRight(initial: R, operation: (Char, R) -> R): R = reverse().fold(initial, {x, y -> operation(y, x)})


/**
 * Applies binary operation to all elements of iterable, going from left to right.
 * Similar to fold function, but uses the first element as initial value
 *
 * @includeFunctionBody ../../test/CollectionTest.kt reduce
 */
public inline fun CharArray.reduce(operation: (Char, Char) -> Char): Char {
    val iterator = this.iterator()!!
    if (!iterator.hasNext()) {
        throw UnsupportedOperationException("Empty iterable can't be reduced")
    }

    var result: Char = iterator.next() //compiler doesn't understand that result will initialized anyway
    while (iterator.hasNext()) {
        result = operation(result, iterator.next())
    }

    return result
}

/**
 * Applies binary operation to all elements of iterable, going from right to left.
 * Similar to foldRight function, but uses the last element as initial value
 *
 * @includeFunctionBody ../../test/CollectionTest.kt reduceRight
 */
public inline fun CharArray.reduceRight(operation: (Char, Char) -> Char): Char = reverse().reduce { x, y -> operation(y, x) }


/**
 * Groups the elements in the collection into a new [[Map]] using the supplied *toKey* function to calculate the key to group the elements by
 *
 * @includeFunctionBody ../../test/CollectionTest.kt groupBy
 */
public inline fun <K> CharArray.groupBy(toKey: (Char) -> K) : Map<K, MutableList<Char>> = groupByTo<K>(HashMap<K, MutableList<Char>>(), toKey)

/**
 * Groups the elements in the collection into the given [[Map]] using the supplied *toKey* function to calculate the key to group the elements by
 *
 * @includeFunctionBody ../../test/CollectionTest.kt groupBy
 */
public inline fun <K> CharArray.groupByTo(result: MutableMap<K, MutableList<Char>>, toKey: (Char) -> K) : Map<K, MutableList<Char>> {
    for (element in this) {
        val key = toKey(element)
        val list = result.getOrPut(key) { ArrayList<Char>() }
        list.add(element)
    }
    return result
}

/**
 * Creates a string from all the elements separated using the *separator* and using the given *prefix* and *postfix* if supplied.
 *
 * If a collection could be huge you can specify a non-negative value of *limit* which will only show a subset of the collection then it will
 * a special *truncated* separator (which defaults to "..."
 *
 * @includeFunctionBody ../../test/CollectionTest.kt makeString
 */
public inline fun CharArray.makeString(separator: String = ", ", prefix: String = "", postfix: String = "", limit: Int = -1, truncated: String = "..."): String {
    val buffer = StringBuilder()
    appendString(buffer, separator, prefix, postfix, limit, truncated)
    return buffer.toString()!!
}

/** Returns a list containing the everything but the first elements that satisfy the given *predicate* */
public inline fun <L: MutableList<Char>> CharArray.dropWhileTo(result: L, predicate: (Char) -> Boolean) : L {
    var start = true
    for (element in this) {
        if (start && predicate(element)) {
            // ignore
        } else {
            start = false
            result.add(element)
        }
    }
    return result
}

/** Returns a list containing the first elements that satisfy the given *predicate* */
public inline fun <C: MutableCollection<Char>> CharArray.takeWhileTo(result: C, predicate: (Char) -> Boolean) : C {
    for (element in this) if (predicate(element)) result.add(element) else break
    return result
}

/** Copies all elements into the given collection */
public inline fun <C: MutableCollection<Char>> CharArray.toCollection(result: C) : C {
    for (element in this) result.add(element)
    return result
}

/**
 * Reverses the order the elements into a list
 *
 * @includeFunctionBody ../../test/CollectionTest.kt reverse
 */
public inline fun CharArray.reverse() : List<Char> {
    val list = toCollection(ArrayList<Char>())
    Collections.reverse(list)
    return list
}

/** Copies all elements into a [[LinkedList]]  */
public inline fun  CharArray.toLinkedList() : LinkedList<Char> = toCollection(LinkedList<Char>())

/** Copies all elements into a [[List]] */
public inline fun  CharArray.toList() : List<Char> = toCollection(ArrayList<Char>())

/** Copies all elements into a [[List] */
public inline fun  CharArray.toCollection() : Collection<Char> = toCollection(ArrayList<Char>())

/** Copies all elements into a [[Set]] */
public inline fun  CharArray.toSet() : Set<Char> = toCollection(HashSet<Char>())

/**
  TODO figure out necessary variance/generics ninja stuff... :)
public inline fun  CharArray.toSortedList(transform: fun(Char) : java.lang.Comparable<*>) : List<Char> {
    val answer = this.toList()
    answer.sort(transform)
    return answer
}
*/
