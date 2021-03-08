rootProject.name = "Minecraft-Toolbox"

include("buildSrc")
findProject(":buildSrc")?.name = "gradle-plugin"

include("base")
include("plugin")