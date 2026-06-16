dependencies {
    api(project(":minecraft-common"))

    // Provided by the Paper server at runtime.
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.66-stable")

    implementation("net.kyori:adventure-platform-bukkit:4.4.1")
}
