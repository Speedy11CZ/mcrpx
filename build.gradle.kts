plugins {
    id("java")
}

group = "cz.speedy11"
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
}

val fatJar = task("fatJar", type = Jar::class) {
    baseName = project.name
    manifest {
        attributes["Implementation-Title"] = rootProject.name
        attributes["Implementation-Version"] = rootProject.version
        attributes["Main-Class"] = "cz.speedy11.mcrpx.Main"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}