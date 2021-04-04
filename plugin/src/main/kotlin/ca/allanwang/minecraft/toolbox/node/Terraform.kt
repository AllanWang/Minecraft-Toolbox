package ca.allanwang.minecraft.toolbox.node

import ca.allanwang.minecraft.toolbox.base.MctNode
import ca.allanwang.minecraft.toolbox.base.PluginScope
import javax.inject.Inject

@PluginScope
class Terraform @Inject internal constructor(
) : MctNode(name = "terraform") {

    @PluginScope
    class Up @Inject internal constructor(
    ) : MctNode(name = "up") {
        override val help: String = "Remove blocks above path"
    }

    @PluginScope
    class Down @Inject internal constructor(
    ) : MctNode(name = "down")

    @PluginScope
    class Fill @Inject internal constructor(
    ) : MctNode(name = "fill")
}