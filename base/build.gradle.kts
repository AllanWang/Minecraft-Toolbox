dependencies {
    implementation(kotlin("reflect"))
    compileOnly(mct.Dependencies.bukkit)
    implementation(mct.Dependencies.coroutines)
    implementation(mct.Dependencies.mccoroutine)
    implementation(mct.Dependencies.dagger)
    kapt(mct.Dependencies.daggerKapt)

    testCompileOnly(mct.Dependencies.bukkit)
}