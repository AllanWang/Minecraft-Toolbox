rootProject.name = "Minecraft-Toolbox"

include("buildSrc")
findProject(":buildSrc")?.name = "gradle-plugin"