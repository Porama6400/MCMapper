plugins {
    java
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "net.otlg"
version = "1.4"

repositories {
    maven("https://files.otlg.net/repositories/maven")
    mavenCentral()
    mavenLocal();
}

dependencies {
    testImplementation("junit", "junit", "4.12")

    implementation("net.otlg:bitumen:2.0.1")

    implementation("commons-cli:commons-cli:1.4");
    implementation("commons-io:commons-io:2.6");

    implementation("com.google.code.gson", "gson", "2.8.6");

    implementation("org.ow2.asm:asm:8.0.1");
    implementation("org.ow2.asm:asm-commons:8.0.1");
    implementation("org.ow2.asm:asm-util:8.0.1");
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

var export = configurations.create("export");
export.extendsFrom(configurations.implementation.get());

tasks.shadowJar {
    this.archiveBaseName.set("MCMapper");
    this.configurations.add(export);
    manifest {
        attributes["Main-Class"] = "net.otlg.mcmapper.MCMapper"
    }
}

tasks.build.get().dependsOn(tasks.shadowJar.get());