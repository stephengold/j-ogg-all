// global build settings shared by all j-ogg-all subprojects

rootProject.name = "j-ogg-all"

dependencyResolutionManagement {
    repositories {
        //mavenLocal() // to find libraries installed locally
        mavenCentral() // to find libraries released to the Maven Central repository
        maven {
            name = "Central Portal Snapshots"
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
}

// subprojects:
include("library")
include("vorbis")
