plugins {
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(rootProject.projects.mcrpxCommon)
    implementation(libs.jopt.simple)
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "cz.speedy11.mcrpx.cli.Main",
        )
    }
}

tasks.shadowJar {
    archiveFileName.set("mcrpx-cli-${rootProject.version}.jar")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

artifacts {
    add("archives", tasks.shadowJar)
}