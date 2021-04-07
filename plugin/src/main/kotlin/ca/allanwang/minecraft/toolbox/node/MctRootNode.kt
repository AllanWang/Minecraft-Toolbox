package ca.allanwang.minecraft.toolbox.node

import ca.allanwang.minecraft.toolbox.base.MctNode
import ca.allanwang.minecraft.toolbox.base.PluginScope
import javax.inject.Inject

@PluginScope
class MctRootNode @Inject internal constructor(
    compass: Compass,
    beacon: Beacon,
    teleport: Teleport
) : MctNode(name = "mct") {

    init {
        children(compass, beacon, teleport)
    }

}
