plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "cz.speedy11"
version = "1.2.0"

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }
}