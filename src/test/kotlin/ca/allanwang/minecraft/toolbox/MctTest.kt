package ca.allanwang.minecraft.toolbox

import org.junit.jupiter.api.Test

class MctTest {

    @Test
    fun mct() {
        Mct(mctTestContext()).main(listOf("help"))
    }
}