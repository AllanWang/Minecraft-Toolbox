import java.util.*

dependencies {
    implementation(rootProject)
    implementation(project(":core"))
    implementation(project(":base"))
    implementation(kotlin("reflect"))
    compileOnly(mct.Dependencies.bukkit)
    implementation(mct.Dependencies.coroutines)
    implementation(mct.Dependencies.mysql)
    implementation(mct.Dependencies.sqldelight("jdbc-driver"))
    api(mct.Dependencies.dagger)
    kapt(mct.Dependencies.daggerKapt)

    testImplementation(kotlin("reflect"))
    testImplementation(mct.Dependencies.h2)
//    testImplementation(mct.Dependencies.guava)
    testImplementation(mct.Dependencies.dagger)
    kaptTest(mct.Dependencies.daggerKapt)
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


