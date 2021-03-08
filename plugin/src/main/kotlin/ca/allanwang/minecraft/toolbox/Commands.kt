package ca.allanwang.minecraft.toolbox

import ca.allanwang.minecraft.toolbox.base.MctNode
import ca.allanwang.minecraft.toolbox.base.PluginScope
import javax.inject.Inject

@PluginScope
class Mct @Inject internal constructor(
    compass: Compass
) : MctNode(name = "mct") {

    init {
        children(compass)
    }

}
