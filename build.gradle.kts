// Gradle script to build the j-ogg-all project

plugins {
    base // to add a "clean" task to the root project
}

ext {
    set("version", "1.0.6")
}

tasks.register("checkstyle") {
    dependsOn(":library:checkstyleMain", ":vorbis:checkstyleMain")
}

// Register publishing tasks:

tasks.register("install") {
    dependsOn(":library:install", "vorbis:install")
    description = "Installs Maven artifacts to the local repository."
}
tasks.register("release") {
    dependsOn("library:release", "vorbis:release")
    description = "Stages Maven artifacts to Sonatype OSSRH."
}
