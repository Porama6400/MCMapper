plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.porama"
version = "1.5"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.jetbrains:annotations:23.0.0")
    testImplementation("junit", "junit", "4.12")

    implementation("commons-cli:commons-cli:1.5.0")
    implementation("commons-io:commons-io:2.11.0")

    implementation("com.google.code.gson", "gson", "2.8.6")

    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")
    implementation("org.ow2.asm:asm-util:9.2")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

var export = configurations.create("export")
export.extendsFrom(configurations.implementation.get())

tasks.shadowJar {
    this.archiveBaseName.set("MCMapper")
    this.configurations.add(export)
    manifest {
        attributes["Main-Class"] = "dev.porama.mcmapper.MCMapper"
    }
}

tasks.build.get().dependsOn(tasks.shadowJar.get())