enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "mcrpx-parent"

setupSubproject("common")
setupSubproject("gui")
setupSubproject("cli")

fun setupSubproject(name: String) {
    setupSubproject("mcrpx-$name") {
        projectDir = file(name)
    }
}

inline fun setupSubproject(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}