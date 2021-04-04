rootProject.name = "Minecraft-Toolbox"

include("buildSrc")
findProject(":buildSrc")?.name = "gradle-plugin"

include("core")
include("base")
include("plugin")