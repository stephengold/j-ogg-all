# J-ogg-all Project

[The J-ogg-all Project][joggall] provides 2 [JVM] libraries
for reading [Ogg] bitstreams and decoding media they contain.

It contains 2 sub-projects:

1. library: builds the full "j-ogg-all" runtime library, including decoders for
   [Vorbis], [Free Lossless Audio Codec (FLAC)][flac], and [Theora] media.
   This library also provides optional interfaces to
   [the Java Media Framework (JMF)][jmf].
2. vorbis: builds a reduced library ("j-ogg-vorbis") for decoding
   Vorbis audio only (no support for FLAC, Theora, or JMF).

Complete source code (in [Java]) is provided under
[an informal license][license].


<a name="toc"></a>

## Contents of this document

+ [Important features](#features)
+ [What's missing](#todo)
+ [How to add j-ogg-all to an existing project](#addall)
+ [How to add j-ogg-vorbis to an existing project](#addvorbis)
+ [How to build the project from source](#build)
+ [Downloads](#downloads)
+ [Conventions](#conventions)
+ [External links](#links)
+ [History](#history)


<a name="features"></a>

## Important features

+ read bitstreams and metadata from [Ogg] containers
+ decode [Vorbis] audio
+ decode [FLAC] audio
+ extract album art from Vorbis comments

[Jump to table of contents](#toc)


<a name="todo"></a>

## What's missing

+ The [Theora] decoder is very incomplete.
+ No decoders are provided for:
  + Constrained Energy Lapped Transform (CELT) audio
  + Continuous Media Markup Language (CMML)
  + [Daala video][daala]
  + [Opus interactive audio][opus]
  + [Speex audio][speex]

[Jump to table of contents](#toc)

<a name="addall"></a>

## How to add j-ogg-all to an existing project

The j-ogg-all library is available pre-built.
It depends on the Java Media Framework.
Adding j-ogg-all to an existing JVM project should be
a simple matter of adding these libraries to the classpath.

For projects built using [Maven] or [Gradle], it is sufficient to add a
dependency on j-ogg-all.
Build tools should automatically resolve the dependency on JMF.

### Gradle-built projects

Add to the project’s "build.gradle" file:

    repositories {
        mavenCentral()
    }
    dependencies {
        implementation 'com.github.stephengold:j-ogg-all:1.0.4'
    }

For some older versions of Gradle,
it's necessary to replace `implementation` with `compile`.

### Maven-built projects

Add to the project’s "pom.xml" file:

    <repositories>
      <repository>
        <id>mvnrepository</id>
        <url>https://repo1.maven.org/maven2/</url>
      </repository>
    </repositories>

    <dependency>
      <groupId>com.github.stephengold</groupId>
      <artifactId>j-ogg-all</artifactId>
      <version>1.0.4</version>
    </dependency>

[Jump to table of contents](#toc)


<a name="addvorbis"></a>

## How to add j-ogg-vorbis to an existing project

The j-ogg-vorbis library is available pre-built.
Adding j-ogg-vorbis to an existing JVM project should be
a simple matter of adding this library to the classpath.

### Gradle-built projects

Add to the project’s "build.gradle" file:

    repositories {
        mavenCentral()
    }
    dependencies {
        implementation 'com.github.stephengold:j-ogg-vorbis:1.0.4'
    }

For some older versions of Gradle,
it's necessary to replace `implementation` with `compile`.

### Maven-built projects

Add to the project’s "pom.xml" file:

    <repositories>
      <repository>
        <id>mvnrepository</id>
        <url>https://repo1.maven.org/maven2/</url>
      </repository>
    </repositories>

    <dependency>
      <groupId>com.github.stephengold</groupId>
      <artifactId>j-ogg-vorbis</artifactId>
      <version>1.0.4</version>
    </dependency>

[Jump to table of contents](#toc)


<a name="build"></a>

## How to build the project from source

1. Install a [Java Development Kit (JDK)][adoptium],
   if you don't already have one.
2. Point the `JAVA_HOME` environment variable to your JDK installation:
   (The path might be something like "C:\Program Files\Java\jre1.8.0_301"
   or "/usr/lib/jvm/java-8-openjdk-amd64/" or
   "/Library/Java/JavaVirtualMachines/liberica-jdk-17-full.jdk/Contents/Home" .)
  + using Bash or Zsh: `export JAVA_HOME="` *path to installation* `"`
  + using Windows Command Prompt: `set JAVA_HOME="` *path to installation* `"`
  + using PowerShell: `$env:JAVA_HOME = '` *path to installation* `'`
3. Download and extract the j-ogg-all source code from [GitHub]:
  + using [Git]:
    + `git clone https://github.com/stephengold/j-ogg-all.git`
    + `cd j-ogg-all`
    + `git checkout -b latest 1.0.4`
  + using a web browser:
    + browse to [the latest release][latest]
    + follow the "Source code (zip)" link
    + save the ZIP file
    + extract the contents of the saved ZIP file
    + `cd` to the extracted directory/folder
4. Run the [Gradle] wrapper:
  + using Bash or PowerShell or Zsh: `./gradlew build`
  + using Windows Command Prompt: `.\gradlew build`

After a successful build,
Maven artifacts will be found
in "library/build/libs" and "vorbis/build/libs".

You can install the artifacts to your local Maven repository:
+ using Bash or PowerShell or Zsh: `./gradlew install`
+ using Windows Command Prompt: `.\gradlew install`

You can restore the project to a pristine state:
+ using Bash or PowerShell or Zsh: `./gradlew clean`
+ using Windows Command Prompt: `.\gradlew clean`

[Jump to table of contents](#toc)


<a name="downloads"></a>

## Downloads

Releases can be downloaded from [GitHub](https://github.com/stephengold/j-ogg-all/releases)
or from the Maven Central Repository:
+ [j-ogg-all](https://central.sonatype.com/artifact/com.github.stephengold/j-ogg-all/1.0.4)
+ [j-ogg-vorbis](https://central.sonatype.com/artifact/com.github.stephengold/j-ogg-vorbis/1.0.4)

[Jump to table of contents](#toc)


<a name="conventions"></a>

## Conventions

Package names begin with `de.jarnbjo.`

The source code is compatible with JDK 7.
The pre-built libraries are compatible with JDK 8.

[Jump to table of contents](#toc)


<a name="links"></a>

## External links

+ [reference implementation of the Ogg container format](https://github.com/xiph/ogg)
+ [reference implementation of the Vorbis codec](https://gitlab.xiph.org/xiph/vorbis)
+ [Vorbis samples](https://getsamplefiles.com/sample-audio-files/ogg)

[Jump to table of contents](#toc)


<a name="history"></a>

## History

The j-ogg-all project was created by Tor-Einar Jarnbjo circa 2002,
probably based on reference implementations in C.

In March 2021, Stephen Gold revived the project at GitHub
and added Gradle build scripts.

In September 2022, Robert Pengelly contributed code
to retrieve album art from Vorbis media.

In February 2023, the "library" and "vorbis" subprojects split off.

[Jump to table of contents](#toc)


[adoptium]: https://adoptium.net/releases.html "Adoptium Project"
[daala]: https://xiph.org/daala/ "Daala codec"
[flac]: https://xiph.org/flac/ "Free Lossless Audio Codec"
[git]: https://git-scm.com "Git"
[github]: https://github.com "GitHub"
[gradle]: https://gradle.org "Gradle Project"
[java]: https://en.wikipedia.org/wiki/Java_(programming_language) "Java"
[jmf]: https://www.oracle.com/java/technologies/javase/java-media-framework.html "Java Media Framework"
[joggall]: https://github.com/stephengold "J-ogg-all Project"
[jvm]: https://en.wikipedia.org/wiki/Java_virtual_machine "Java Virtual Machine"
[latest]: https://github.com/stephengold/j-ogg-all/releases/latest "latest release"
[license]: https://github.com/stephengold/j-ogg-all/blob/master/LICENSE "j-ogg-all license"
[maven]: https://maven.apache.org "Maven Project"
[ogg]: https://www.xiph.org/ogg/ "Ogg container format"
[opus]: https://opus-codec.org/ "Opus codec"
[speex]: https://speex.org/ "Speex codec"
[theora]: https://theora.org/ "Theora codec"
[vorbis]: https://xiph.org/vorbis/ "Vorbis codec"
