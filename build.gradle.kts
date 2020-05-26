plugins {
    java
    id("com.github.johnrengelman.shadow") version "5.2.0"
    maven
    `maven-publish`
}

group = "net.otlg"
version = "1.3"

repositories {
    mavenCentral()
    maven("https://files.otlg.net/maven-repo/");
}


var export = configurations.create("export");
configurations.implementation.get().extendsFrom(export);

dependencies {
    testImplementation("junit", "junit", "4.12")

    export("net.otlg:bitumen:1.0+")

    export("commons-cli:commons-cli:1.4");
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

publishing {
    repositories {
        maven {
            name = "GithubPackage"
            url = uri("https://maven.pkg.github.com/Porama6400/MCMapper")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("GithubPackage") {
            from(components["java"])
        }
    }
}