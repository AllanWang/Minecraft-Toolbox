package ca.allanwang.minecraft.toolbox

import com.github.ajalt.clikt.core.subcommands

class Mct(override val mctContext: MctContext) : MctCommand() {

    init {
        subcommands(Help(mctContext))
    }

}

class Help(override val mctContext: MctContext) : MctCommand() {

    override fun MctContext.run() {
        logger.info("Sent help")
        echo("Sent help")
    }
}
