plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "5.2.0";
    id("org.openjfx.javafxplugin") version "0.0.9"
}

javafx {
    version = "15.0.1"
    modules("javafx.base", "javafx.controls", "javafx.fxml");
}

group = "net.otlg"
version = "1.2"

repositories {
    mavenCentral()
    maven("https://files.otlg.net/repositories/maven")
}

dependencies {
    testImplementation("junit", "junit", "4.12")

    implementation("net.otlg:bitumen:2.0.1")
    implementation(project(":core"))

    runtimeOnly("org.openjfx:javafx-base:$javafx.version:win");
    runtimeOnly("org.openjfx:javafx-controls:$javafx.version:win");
    runtimeOnly("org.openjfx:javafx-fxml:$javafx.version:win");
    runtimeOnly("org.openjfx:javafx-graphics:$javafx.version:win");
}


var export = configurations.create("export");
export.extendsFrom(configurations.implementation.get());


application {
    mainClassName = "net.otlg.mcmapper.gui.MCMapperGUI"
}

tasks.shadowJar {


    this.archiveBaseName.set("MCMapperGUI");
    this.configurations.add(export);

//    manifest {
//        attributes["Main-Class"] = "net.otlg.mcmapper.gui.MCMapperGUI"
//    }
}

tasks.build.get().dependsOn(tasks.shadowJar.get());