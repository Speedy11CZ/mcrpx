plugins {
    id("com.github.johnrengelman.shadow")
    id("org.openjfx.javafxplugin") version "0.0.14"
}

dependencies {
    implementation(rootProject.projects.mcrpxCommon)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "cz.speedy11.mcrpx.gui.Main"
        attributes["Specification-Title"] = rootProject.name
        attributes["Specification-Version"] = rootProject.version
    }
}

tasks.shadowJar {
    archiveFileName.set("mcrpx-gui-${rootProject.version}.jar")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

artifacts {
    add("archives", tasks.shadowJar)
}

javafx {
    modules = listOf("javafx.base", "javafx.graphics", "javafx.controls")
}