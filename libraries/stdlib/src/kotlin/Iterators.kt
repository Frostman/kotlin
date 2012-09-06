package kotlin

import kotlin.support.*
import java.util.Collections

/**
 * Returns an iterator which invokes the function to calculate the next value on each iteration until the function returns *null*
 *
 * @includeFunctionBody ../../test/iterators/IteratorsTest.kt fibonacci
 */
public inline fun <T> iterate(nextFunction: () -> T?) : Iterator<T> = FunctionIterator(nextFunction)

/**
 * Returns an iterator over elements which match the given *predicate*
 *
 * @includeFunctionBody ../../test/iterators/IteratorsTest.kt filterAndTakeWhileExtractTheElementsWithinRange
 */
public inline fun <T> Iterator<T>.filter(predicate: (T) -> Boolean) : Iterator<T> = FilterIterator<T>(this, predicate)

private class FilterIterator<T>(val iterator : Iterator<T>, val predicate: (T)-> Boolean) : AbstractIterator<T>() {
    override protected fun computeNext(): Unit {
        while (iterator.hasNext()) {
            val next = iterator.next()
            if ((predicate)(next)) {
                setNext(next)
                return
            }
        }
        done()
    }
}

/** Returns an iterator over elements which do not match the given *predicate* */
public inline fun <T> Iterator<T>.filterNot(predicate: (T) -> Boolean) : Iterator<T> = filter { !predicate(it) }

/** Returns an iterator over non-*null* elements */
public inline fun <T> Iterator<T?>?.filterNotNull() : Iterator<T> = FilterNotNullIterator(this)

private class FilterNotNullIterator<T>(val iterator : Iterator<T?>?) : AbstractIterator<T>() {
    override protected fun computeNext(): Unit {
        if (iterator != null) {
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next != null) {
                    setNext(next)
                    return
                }
            }
        }
        done()
    }
}

/**
 * Returns an iterator obtained by applying *transform*, a function transforming an object of type *T* into an object of type *R*
 *
 * @includeFunctionBody ../../test/iterators/IteratorsTest.kt mapAndTakeWhileExtractTheTransformedElements
 */
public inline fun <T, R> Iterator<T>.map(transform: (T) -> R): Iterator<R> = MapIterator<T, R>(this, transform)

private class MapIterator<T, R>(val iterator : Iterator<T>, val transform: (T) -> R) : AbstractIterator<R>() {
    override protected fun computeNext() : Unit {
        if (iterator.hasNext()) {
            setNext((transform)(iterator.next()))
        } else {
            done()
        }
    }
}

/**
 * Returns an iterator over the concatenated results of transforming each element to one or more values
 *
 * @includeFunctionBody ../../test/iterators/IteratorsTest.kt flatMapAndTakeExtractTheTransformedElements
 */
public inline fun <T, R> Iterator<T>.flatMap(transform: (T) -> Iterator<R>): Iterator<R> = FlatMapIterator<T, R>(this, transform)

private class FlatMapIterator<T, R>(val iterator : Iterator<T>, val transform: (T) -> Iterator<R>) : AbstractIterator<R>() {
    var transformed: Iterator<R> = iterate<R> { null }

    override protected fun computeNext() : Unit {
        while (true) {
            if (transformed.hasNext()) {
                setNext(transformed.next())
                return
            }
            if (iterator.hasNext()) {
                transformed = (transform)(iterator.next())
            } else {
                done()
                return
            }
        }
    }
}

/**
 * Creates an [[Iterator]] which iterates over this iterator then the given element at the end
 *
 * @includeFunctionBody ../../test/iterators/IteratorsTest.kt plus
 */
public inline fun <in T> Iterator<T>.plus(element: T): Iterator<T> {
    return CompositeIterator<T>(this, SingleIterator(element))
}


/**
 * Creates an [[Iterator]] which iterates over this iterator then the following iterator
 *
 * @includeFunctionBody ../../test/iterators/IteratorsTest.kt plusCollection
 */
public inline fun <in T> Iterator<T>.plus(iterator: Iterator<T>): Iterator<T> {
    return CompositeIterator<T>(this, iterator)
}

/**
 * Creates an [[Iterator]] which iterates over this iterator then the following collection
 *
 * @includeFunctionBody ../../test/iterators/IteratorsTest.kt plusCollection
 */
public inline fun <in T> Iterator<T>.plus(collection: Iterable<T>): Iterator<T> = plus(collection.iterator())

/**
 * Returns an iterator containing all the non-*null* elements, lazily throwing an [[IllegalArgumentException]]
 if there are any null elements
 *
 * @includeFunctionBody ../../test/iterators/IteratorsTest.kt requireNoNulls
 */
public inline fun <in T> Iterator<T?>.requireNoNulls(): Iterator<T> {
    return map<T?, T>{
        if (it == null) throw IllegalArgumentException("null element in iterator $this") else it
    }
}


/**
 * Returns an iterator restricted to the first *n* elements
 *
 * @includeFunctionBody ../../test/iterators/IteratorsTest.kt takeExtractsTheFirstNElements
 */
public inline fun <T> Iterator<T>.take(n: Int): Iterator<T> {
    var count = n
    return takeWhile{ --count >= 0 }
}

/**
 * Returns an iterator restricted to the first elements that match the given *predicate*
 *
 * @includeFunctionBody ../../test/iterators/IteratorsTest.kt filterAndTakeWhileExtractTheElementsWithinRange
 */
public inline fun <T> Iterator<T>.takeWhile(predicate: (T) -> Boolean): Iterator<T> = TakeWhileIterator<T>(this, predicate)

private class TakeWhileIterator<T>(val iterator: Iterator<T>, val predicate: (T) -> Boolean) : AbstractIterator<T>() {
    override protected fun computeNext() : Unit {
        if (iterator.hasNext()) {
            val item = iterator.next()
            if ((predicate)(item)) {
                setNext(item)
                return
            }
        }
        done()
    }
}
