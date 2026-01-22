plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.0"
}

java.sourceCompatibility = JavaVersion.VERSION_21

group = "com.shanebeestudios"
val projectVersion = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(files("/Users/ShaneBee/Desktop/Server/Hytale/Assets/HytaleServer.jar"))
    compileOnly("org.jetbrains:annotations:26.0.2")
    implementation("com.github.SkriptDev:skript-parser:master-SNAPSHOT")
}

tasks {
    register("survival-server", Copy::class) {
        dependsOn("jar")
        from("build/libs") {
            include("HySkript-*.jar")
            destinationDir = file("/Users/ShaneBee/Desktop/Server/Hytale/Survival/mods/")
        }
    }
    register("creative-server", Copy::class) {
        dependsOn("jar")
        from("build/libs") {
            include("HySkript-*.jar")
            destinationDir = file("/Users/ShaneBee/Desktop/Server/Hytale/Creative/mods/")
        }
    }
    processResources {
        filesNotMatching("assets/**") {
            expand("pluginVersion" to projectVersion)
        }
    }
    compileJava {
        options.release = 21
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }
    shadowJar {
        archiveFileName = project.name + "-" + projectVersion + ".jar"
        relocate("io.github.syst3ms", "com.shanebeestudios.skript.base")
    }
    jar {
        dependsOn(shadowJar)
    }
}
