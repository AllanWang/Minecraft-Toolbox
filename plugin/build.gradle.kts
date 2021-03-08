import java.util.Properties

dependencies {
    implementation(project(":base"))
    implementation(kotlin("reflect"))
    compileOnly(mct.Dependencies.bukkit)
    implementation(mct.Dependencies.mccoroutine)
    implementation(mct.Dependencies.mccoroutine("core"))
    implementation(mct.Dependencies.dagger)
    kapt(mct.Dependencies.daggerKapt)

    testCompileOnly(mct.Dependencies.bukkit)
}

val fatJar = task("fatJar", type = Jar::class) {
    archiveBaseName.set("Minecraft-Toolbox-Release")
    archiveVersion.set("")
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    from(
        configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

task("exportJar", type = Copy::class) {
    val props = Properties()
    rootProject.file("priv.properties").inputStream().use { props.load(it) }

    from(fatJar.archiveFile)
    into(props.getProperty("export_folder"))
    dependsOn(fatJar)
}