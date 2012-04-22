// NOTE this file is auto-generated from src/kotlin/JLangIterables.kt
package kotlin

import kotlin.util.*

import java.util.*

/**
 * Returns *true* if all elements match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt all
 */
public inline fun IntArray.all(predicate: (Int) -> Boolean) : Boolean {
    for (element in this) if (!predicate(element)) return false
    return true
}

/**
 * Returns *true* if any elements match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt any
 */
public inline fun IntArray.any(predicate: (Int) -> Boolean) : Boolean {
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
public inline fun IntArray.appendString(buffer: Appendable, separator: String = ", ", prefix: String = "", postfix: String = "", limit: Int = -1, truncated: String = "..."): Unit {
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
public inline fun IntArray.count(predicate: (Int) -> Boolean) : Int {
    var count = 0
    for (element in this) if (predicate(element)) count++
    return count
}

/**
 * Returns the first element which matches the given *predicate* or *null* if none matched
 *
 * @includeFunctionBody ../../test/CollectionTest.kt find
 */
public inline fun IntArray.find(predicate: (Int) -> Boolean) : Int? {
    for (element in this) if (predicate(element)) return element
    return null
}

/**
 * Filters all elements which match the given predicate into the given list
 *
 * @includeFunctionBody ../../test/CollectionTest.kt filterIntoLinkedList
 */
public inline fun <C: Collection<Int>> IntArray.filterTo(result: C, predicate: (Int) -> Boolean) : C {
    for (element in this) if (predicate(element)) result.add(element)
    return result
}

/**
 * Returns a list containing all elements which do not match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt filterNotIntoLinkedList
 */
public inline fun <L: List<Int>> IntArray.filterNotTo(result: L, predicate: (Int) -> Boolean) : L {
    for (element in this) if (!predicate(element)) result.add(element)
    return result
}

/**
 * Filters all non-*null* elements into the given list
 *
 * @includeFunctionBody ../../test/CollectionTest.kt filterNotNullIntoLinkedList
 */
public inline fun <L: List<Int>> IntArray?.filterNotNullTo(result: L) : L {
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
public inline fun <R> IntArray.flatMapTo(result: Collection<R>, transform: (Int) -> Collection<R>) : Collection<R> {
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
public inline fun IntArray.forEach(operation: (Int) -> Unit) : Unit = for (element in this) operation(element)

/**
 * Folds all elements from from left to right with the *initial* value to perform the operation on sequential pairs of elements
 *
 * @includeFunctionBody ../../test/CollectionTest.kt fold
 */
public inline fun IntArray.fold(initial: Int, operation: (Int, Int) -> Int): Int {
    var answer = initial
    for (element in this) answer = operation(answer, element)
    return answer
}

/**
 * Folds all elements from right to left with the *initial* value to perform the operation on sequential pairs of elements
 *
 * @includeFunctionBody ../../test/CollectionTest.kt foldRight
 */
public inline fun IntArray.foldRight(initial: Int, operation: (Int, Int) -> Int): Int = reverse().fold(initial, operation)

/**
 * Groups the elements in the collection into a new [[Map]] using the supplied *toKey* function to calculate the key to group the elements by
 *
 * @includeFunctionBody ../../test/CollectionTest.kt groupBy
 */
public inline fun <K> IntArray.groupBy(toKey: (Int) -> K) : Map<K, List<Int>> = groupByTo<K>(HashMap<K, List<Int>>(), toKey)

/**
 * Groups the elements in the collection into the given [[Map]] using the supplied *toKey* function to calculate the key to group the elements by
 *
 * @includeFunctionBody ../../test/CollectionTest.kt groupBy
 */
public inline fun <K> IntArray.groupByTo(result: Map<K, List<Int>>, toKey: (Int) -> K) : Map<K, List<Int>> {
    for (element in this) {
        val key = toKey(element)
        val list = result.getOrPut(key) { ArrayList<Int>() }
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
public inline fun IntArray.makeString(separator: String = ", ", prefix: String = "", postfix: String = "", limit: Int = -1, truncated: String = "..."): String {
    val buffer = StringBuilder()
    appendString(buffer, separator, prefix, postfix, limit, truncated)
    return buffer.toString()!!
}

/** Returns a list containing the first elements that satisfy the given *predicate* */
public inline fun <L: List<Int>> IntArray.takeWhileTo(result: L, predicate: (Int) -> Boolean) : L {
    for (element in this) if (predicate(element)) result.add(element) else break
    return result
}

/**
 * Reverses the order the elements into a list
 *
 * @includeFunctionBody ../../test/CollectionTest.kt reverse
 */
public inline fun IntArray.reverse() : List<Int> {
    val answer = LinkedList<Int>()
    for (element in this) answer.addFirst(element)
    return answer
}

/** Copies all elements into the given collection */
public inline fun <C: Collection<Int>> IntArray.toCollection(result: C) : C {
    for (element in this) result.add(element)
    return result
}

/** Copies all elements into a [[LinkedList]]  */
public inline fun  IntArray.toLinkedList() : LinkedList<Int> = toCollection(LinkedList<Int>())

/** Copies all elements into a [[List]] */
public inline fun  IntArray.toList() : List<Int> = toCollection(ArrayList<Int>())

/** Copies all elements into a [[List] */
public inline fun  IntArray.toCollection() : Collection<Int> = toCollection(ArrayList<Int>())

/** Copies all elements into a [[Set]] */
public inline fun  IntArray.toSet() : Set<Int> = toCollection(HashSet<Int>())

/** Copies all elements into a [[SortedSet]] */
public inline fun  IntArray.toSortedSet() : SortedSet<Int> = toCollection(TreeSet<Int>())

/**
  TODO figure out necessary variance/generics ninja stuff... :)
public inline fun  IntArray.toSortedList(transform: fun(Int) : java.lang.Comparable<*>) : List<Int> {
    val answer = this.toList()
    answer.sort(transform)
    return answer
}
*/
