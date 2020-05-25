plugins {
    java
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "net.otlg"
version = "1.3"

repositories {
    mavenCentral()
}


var export = configurations.create("export");
configurations.implementation.get().extendsFrom(export);

dependencies {
    testImplementation("junit", "junit", "4.12")

    export("commons-cli:commons-cli:1.4");
    export("org.jetbrains:annotations:19.0.0");
    export("commons-io:commons-io:2.6");

    export("org.ow2.asm:asm:8.0.1");
    export("org.ow2.asm:asm-commons:8.0.1");
    export("org.ow2.asm:asm-util:8.0.1");

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.shadowJar {
    this.configurations.add(export);
    manifest {
        attributes["Main-Class"] = "net.otlg.mcmapper.MCMapper"
    }
}