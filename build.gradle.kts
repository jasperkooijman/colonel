plugins {
    `java-library`
    `maven-publish`
}

group = "com.milomade0.colonel"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()

        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }

        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    dependencies {
        api("org.jetbrains:annotations:24.1.0")

        testImplementation(platform("org.junit:junit-bom:6.0.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])

                groupId = rootProject.group.toString()
                artifactId = project.name
                version = project.version.toString()
            }
        }

        repositories {
            mavenLocal()
        }
    }
}