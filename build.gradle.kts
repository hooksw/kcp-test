plugins {
    kotlin("jvm") version "1.9.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.10")

//    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
    testImplementation(fileTree("libs",){
        include("*.jar")
    })
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}
