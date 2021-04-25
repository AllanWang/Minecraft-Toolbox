package ca.allanwang.minecraft.toolbox.core

import java.awt.Point

inline fun <T, K, V> Iterable<T>.groupBySet(
    keySelector: (T) -> K,
    valueTransform: (T) -> V
): Map<K, Set<V>> {
    return groupBySetTo(LinkedHashMap(), keySelector, valueTransform)
}

/**
 * Copy of groupBy, where list is replaced by set.
 */
inline fun <T, K, V, M : MutableMap<in K, MutableSet<V>>> Iterable<T>.groupBySetTo(
    destination: M,
    keySelector: (T) -> K,
    valueTransform: (T) -> V
): M {
    for (element in this) {
        val key = keySelector(element)
        val set = destination.getOrPut(key) { mutableSetOf() }
        set.add(valueTransform(element))
    }
    return destination
}

fun Point.coord(): String = "($x, $y)"

fun min(a: Int, b: Int, vararg nums: Int) =
    nums.fold(kotlin.math.min(a, b)) { acc, c -> kotlin.math.min(acc, c) }

fun max(a: Int, b: Int, vararg nums: Int) =
    nums.fold(kotlin.math.max(a, b)) { acc, c -> kotlin.math.max(acc, c) }

@Suppress("UNCHECKED_CAST")
fun <T : Any> Sequence<T?>.untilFirstNull(): Sequence<T> =
    takeWhile { it != null } as Sequence<T>