package ca.allanwang.minecraft.toolbox

import com.github.ajalt.clikt.core.subcommands

class Mct : MctCommand() {

    init {
        subcommands(Help())
    }

    override fun run() = Unit

}

class Help : MctCommand() {

    override fun run() {
        echo("Sent help")
    }
}
