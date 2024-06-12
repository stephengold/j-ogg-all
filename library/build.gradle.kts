// Gradle script to build the "library" subproject of j-ogg-all

plugins {
    `checkstyle`    // to analyze Java sourcecode for style violations
    `java-library`  // to build JVM libraries
    `maven-publish` // to publish artifacts to Maven repositories
    `signing`       // to sign artifacts for publication
}

val group = "com.github.stephengold"
val artifact = "j-ogg-all"
val libraryVersion = rootProject.ext["version"] as String
val baseName = "${artifact}-${libraryVersion}" // for artifacts
val javaVendor = System.getProperty("java.vendor")
val javaVersion = JavaVersion.current()
val websiteUrl = "https://github.com/stephengold/j-ogg-all"

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_8
}

// Incorporate all the classes and resources of the j-ogg-vorbis library:
sourceSets.main {
    java.srcDir("../vorbis/src/main/java")
    resources.srcDir("../vorbis/src/main/resources")
}

dependencies {
    implementation(libs.jmf)
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
}

tasks.withType<JavaCompile>().all { // Java compile-time options:
    options.compilerArgs.add("-Xdiags:verbose")
    if (javaVersion.isCompatibleWith(JavaVersion.VERSION_14)) {
        // Suppress warnings that source value 7 is obsolete.
        options.compilerArgs.add("-Xlint:-options")
    }
    options.compilerArgs.add("-Xlint:unchecked")
    options.setDeprecation(true) // to provide detailed deprecation warnings
    options.encoding = "UTF-8"
    if (javaVersion.isCompatibleWith(JavaVersion.VERSION_1_10)) {
        options.release = 8
    }
}

// TODO Disable doclint for JDK8+

// Register publishing tasks:

tasks.register("install") {
    dependsOn("publishMavenPublicationToMavenLocal")
    description = "Installs the Maven artifacts to the local repository."
}
tasks.register("release") {
    dependsOn("publishMavenPublicationToOSSRHRepository")
    description = "Stages the Maven artifacts to Sonatype OSSRH."
}

tasks.jar {
    archiveBaseName.set(baseName)
    doLast {
        println("built using Java $javaVersion ($javaVendor)")
    }
    manifest {
        attributes["Created-By"] = "$javaVersion ($javaVendor)"
    }
}

java.withJavadocJar()
tasks.named<Jar>("javadocJar") { archiveBaseName.set(baseName) }
tasks.register<Jar>("sourcesJar") {
    archiveBaseName.set(baseName)
    archiveClassifier.set("sources")
    description = "Creates a JAR of Java sourcecode."
    from(sourceSets.main.get().java) // default is ".allSource", which includes resources
}

tasks.named("assemble") { dependsOn("module", "moduleAsc", "pom", "pomAsc") }
tasks.register<Copy>("module") {
    dependsOn("generateMetadataFileForMavenPublication")
    description = "Copies the module metadata to build/libs."
    from("build/publications/maven/module.json")
    into("build/libs")
    rename("module.json", baseName + ".module")
}
tasks.register<Copy>("moduleAsc") {
    dependsOn("signMavenPublication")
    description = "Copies the signature of the module metadata to build/libs."
    from("build/publications/maven/module.json.asc")
    into("build/libs")
    rename("module.json.asc", baseName + ".module.asc")
}
tasks.register<Copy>("pom") {
    dependsOn("generatePomFileForMavenPublication")
    description = "Copies the Maven POM to build/libs."
    from("build/publications/maven/pom-default.xml")
    into("build/libs")
    rename("pom-default.xml", baseName + ".pom")
}
tasks.register<Copy>("pomAsc") {
    dependsOn("signMavenPublication")
    description = "Copies the signature of the Maven POM to build/libs."
    from("build/publications/maven/pom-default.xml.asc")
    into("build/libs")
    rename("pom-default.xml.asc", baseName + ".pom.asc")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.named("sourcesJar"))
            artifactId = artifact
            from(components["java"])
            groupId = group
            pom {
                description = "J-Ogg is a collection of Java libraries for reading Ogg files and decoding different contained formats."
                developers {
                    developer {
                        name = "Tor-Einar Jarnbjo"
                    }
                }
                inceptionYear = "2002"
                licenses {
                    license {
                        distribution = "repo"
                        name = "Free and commercial use, modification and redistribution with attribution."
                        url = websiteUrl + "/blob/master/LICENSE"
                    }
                }
                name = "${group}:${artifact}"
                scm {
                    connection = "scm:git:git://github.com/stephengold/j-ogg-all.git"
                    developerConnection = "scm:git:ssh://github.com:stephengold/j-ogg-all.git"
                    url = websiteUrl + "/tree/master"
                }
                url = websiteUrl
            }
            version = libraryVersion
        }
    }
    // Staging to OSSRH relies on the existence of 2 properties
    // (ossrhUsername and ossrhPassword)
    // which should be stored in ~/.gradle/gradle.properties
    repositories {
        maven {
            credentials {
                username = if (hasProperty("ossrhUsername")) property("ossrhUsername") as String else "Unknown user"
                password = if (hasProperty("ossrhPassword")) property("ossrhPassword") as String else "Unknown password"
            }
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
        }
    }
}
tasks.named("generateMetadataFileForMavenPublication") { dependsOn("pom") }
tasks.named("publishMavenPublicationToMavenLocal") {
    dependsOn("assemble")
    doLast { println("installed locally as " + baseName) }
}
tasks.named("publishMavenPublicationToOSSRHRepository") { dependsOn("assemble") }

// Register signing tasks:

// Signing relies on the existence of 3 properties
// (signing.keyId, signing.password, and signing.secretKeyRingFile)
// which should be stored in ~/.gradle/gradle.properties

signing {
    sign(publishing.publications["maven"])
}
tasks.withType<Sign>().all {
    onlyIf { project.hasProperty("signing.keyId") }
}
tasks.named("signMavenPublication") { dependsOn("module") }
