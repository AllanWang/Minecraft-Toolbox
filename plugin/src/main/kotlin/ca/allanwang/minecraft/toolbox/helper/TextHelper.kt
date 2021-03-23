package ca.allanwang.minecraft.toolbox.helper

fun Iterable<Pair<String, String?>>.prettyJoin(minSpace: Int = 1): List<String> {
    val maxLength = maxOfOrNull { it.first.length } ?: 0
    val expectedSize = maxLength + minSpace
    return map {
        if (it.second == null) it.first
        else buildString {
            append(it.first)
            repeat(expectedSize - it.first.length) {
                append(' ')
            }
            append(it.second)
        }
    }
}

fun List<String>.numberedList(): List<String> =
    mapIndexed { index, s -> "${index + 1}." to s }.prettyJoin()