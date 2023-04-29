plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.thegatesdev"
version = "1.1"
description = "Read and store maple data"
java.sourceCompatibility = JavaVersion.VERSION_17

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    maven("https://jitpack.io")
}

dependencies {
    api("com.github.thegatesdev:maple:v1.4.2")
}

tasks{
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    shadowJar{
        minimize()
        dependencies{
            include(dependency("com.github.thegatesdev:maple"))
        }
    }
}