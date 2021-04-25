package ca.allanwang.minecraft.toolbox.node

import ca.allanwang.minecraft.toolbox.base.CommandContext
import ca.allanwang.minecraft.toolbox.base.MctNode
import ca.allanwang.minecraft.toolbox.base.PluginScope
import javax.inject.Inject

@PluginScope
class Template @Inject internal constructor(
    icePath: IcePath
) : MctNode(name = "template") {

    init {
        children(icePath)
    }

    @PluginScope
    class IcePath @Inject internal constructor(

    ) : MctNode(name = "icepath") {
        override suspend fun CommandContext.command() {

        }
    }

}