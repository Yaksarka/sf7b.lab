plugins { kotlin("jvm"); kotlin("plugin.serialization") }
kotlin { jvmToolchain(17) }
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    testImplementation("junit:junit:4.13.2")
}
tasks.test {
    maxHeapSize="768m"
    systemProperty("sf7b.reportDir",layout.buildDirectory.dir("verification").get().asFile.path)
    systemProperty("sf7b.benchmark",providers.gradleProperty("benchmark").orElse("false").get())
}
