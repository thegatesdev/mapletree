plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.thegatesdev"
version = "1.1"
description = "mapletree"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    api("com.github.thegatesdev:maple:v1.4.2")
}

tasks{
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17)
    }

    shadowJar{
        minimize()
        dependencies{
            include(dependency("com.github.thegatesdev:maple"))
        }
    }
}