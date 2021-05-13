plugins {
    java
    id("com.github.johnrengelman.shadow") version "5.2.0"
    `maven-publish`
}

group = "net.otlg"
version = "1.3"

repositories {
    maven("https://files.otlg.net/repositories/maven")
    mavenCentral()
}


var export = configurations.create("export");
export.extendsFrom(configurations.implementation.get());

dependencies {
    testImplementation("junit", "junit", "4.12")

    implementation("net.otlg:bitumen:1.0+")

    implementation("commons-cli:commons-cli:1.4");
    implementation("commons-io:commons-io:2.6");

    implementation("org.ow2.asm:asm:8.0.1");
    implementation("org.ow2.asm:asm-commons:8.0.1");
    implementation("org.ow2.asm:asm-util:8.0.1");
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

if (System.getenv("USERNAME") == null) {
    println("WARN: Username environment variable is not defined!");
}

if(System.getenv("TOKEN") == null){
    println("WARN: Token environment variable is not defined!");
}

publishing {
    repositories {
        maven {
            name = "Github"
            url = uri("https://maven.pkg.github.com/Porama6400/MCMapper")
            credentials {
                username = System.getenv("USERNAME")
                password = System.getenv("TOKEN")
            }
        }
        maven {
            name = "buildFiles"
            url = uri("$buildDir/repo/")
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}