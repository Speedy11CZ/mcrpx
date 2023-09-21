plugins {
    id("java")
    alias(libs.plugins.shadow)
}

allprojects {
    apply(plugin = "java")

    group = "cz.speedy11"
    version = property("projectVersion") as String // from gradle.properties
    description = property("projectDescription") as String // from gradle.properties

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    repositories {
        mavenCentral()
    }
}