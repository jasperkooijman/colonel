dependencies {
    api(project(":minecraft-common"))

    // Provided by the Paper server at runtime.
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    implementation("net.kyori:adventure-platform-bukkit:4.4.1")
}
