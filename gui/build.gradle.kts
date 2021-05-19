plugins {
    java
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "net.otlg"
version = "1.1.1"


repositories {
    mavenCentral()
    maven("https://files.otlg.net/repositories/maven")
}

dependencies {
    testImplementation("junit", "junit", "4.12")

    implementation("com.google.code.gson", "gson", "2.8.6");
    implementation("net.otlg:bitumen:2.0.1")
//    implementation(project(":core"))
}


var export = configurations.create("export");
export.extendsFrom(configurations.implementation.get());

tasks.shadowJar {
    this.archiveBaseName.set("MCMapperGUI");
    this.configurations.add(export);
    manifest {
        attributes["Main-Class"] = "net.otlg.mcmapper.gui.MCMapperGUI"
    }
}

tasks.build.get().dependsOn(tasks.shadowJar.get());