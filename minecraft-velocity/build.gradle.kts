dependencies {
    api(project(":minecraft-common"))

    // Provided by the Velocity proxy at runtime.
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
}
