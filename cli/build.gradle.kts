plugins {
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":common"))
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
}

tasks.shadowJar {
    archiveFileName.set("MCRPX-cli-${rootProject.version}.jar")
}

tasks {
    "build" {
        dependsOn(shadowJar)
    }
}

artifacts {
    add("archives", tasks.shadowJar)
}