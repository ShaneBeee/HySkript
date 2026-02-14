plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.0"
    id("maven-publish")
    id("checkstyle")
}

java.sourceCompatibility = JavaVersion.VERSION_25

group = "com.github.SkriptDev"
val projectVersion = "1.0.0-pre-release-3"
val hytaleVersion = "2026.02.06-aa1b071c2"
// You can find Hytale versions on their maven repo:
// https://maven.hytale.com/release/com/hypixel/hytale/Server/maven-metadata.xml
// https://maven.hytale.com/pre-release/com/hypixel/hytale/Server/maven-metadata.xml
// (Pre-releases shouldn't be used for production)

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://maven.hytale.com/release")
    maven("https://maven.hytale.com/pre-release")
}

dependencies {
    compileOnly("com.hypixel.hytale:Server:${hytaleVersion}")
    compileOnly("org.jetbrains:annotations:26.0.2")
    implementation("com.github.SkriptDev:skript-parser:1.0.8") {
        isTransitive = false
    }
    implementation("com.github.Zoltus:TinyMessage:2.0.1") {
        isTransitive = false
    }
}

tasks {
    register("server", Copy::class) {
        dependsOn("jar")
        from("build/libs") {
            include("HySkript-*.jar")
            destinationDir = file("/Users/ShaneBee/Desktop/Server/Hytale/Creative/mods/")
        }
    }
    processResources {
        filesNotMatching("assets/**") {
            expand("pluginVersion" to projectVersion, "hytaleVersion" to hytaleVersion)
        }
    }
    compileJava {
        options.release = 25
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }
    shadowJar {
        archiveFileName = project.name + "-" + projectVersion + ".jar"
        archiveClassifier.set("")
        relocate("io.github.syst3ms", "com.github.skriptdev.skript")
        relocate("fi.sulku.hytale", "com.github.skriptdev.skript.tinymessage")
    }
    jar {
        dependsOn(shadowJar)
    }
    register("sourcesJar", Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }
    javadoc {
        title = "HySkript API - $projectVersion"
        options.overview = "src/main/javadoc/overview.html"
        options.encoding = Charsets.UTF_8.name()
        exclude(
            "com/github/skriptdev/skript/plugin/elements",
            "com/github/skriptdev/skript/plugin/command"
        )
        (options as StandardJavadocDocletOptions).links(
            "https://javadoc.io/doc/org.jetbrains/annotations/latest/",
            "https://skriptdev.github.io/docs/skript-parser/latest/"
        )
    }
}

tasks.withType<Checkstyle> {
    // This one class is causing an issue on like 37 (not sure why)
    exclude("**/Block.java")
}

checkstyle {
    // Specify the version of the Checkstyle tool to use
    toolVersion = "10.21.0" // Use a recent version of Checkstyle

    isIgnoreFailures = false

    // Point to your custom Checkstyle configuration file
    // The default location is config/checkstyle/checkstyleMain.xml
    configFile = file("${rootProject.projectDir}/config/checkstyle/checkstyle.xml")
}

publishing {
    publications {
        create<MavenPublication>("mavenShadow") {
            // Use the "shadow" component to include the shadowed JAR and its dependencies in the POM
            from(components["shadow"])

            // Add the sources JAR as an additional artifact
            artifact(tasks["sourcesJar"])
            group = "com.github.SkriptDev"
            version = projectVersion
            artifactId = "HySkript"
        }
    }
}
