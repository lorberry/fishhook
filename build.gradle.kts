plugins {
    id("java")
}

group = "dev.lorberry"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.register<Jar>("agentJar") {
    from(sourceSets.main.get().output) {
        include("dev/lorberry/fishhook/agent/**")
        include("dev/lorberry/fishhook/client/**")
    }
    archiveFileName.set("agent.jar")
    manifest {
        attributes(
            "Agent-Class" to "dev.lorberry.fishhook.agent.Main",
            "Can-Redefine-Classes" to "true",
            "Can-Retransform-Classes" to "true"
        )
    }
}

tasks.register<Jar>("loaderJar") {
    from(sourceSets.main.get().output) {
        include("dev/lorberry/fishhook/loader/**")
    }
    archiveFileName.set("loader.jar")
    manifest {
        attributes("Main-Class" to "dev.lorberry.fishhook.loader.Loader")
    }
}

tasks.named("jar") {
    enabled = false
}

tasks.build {
    dependsOn("agentJar", "loaderJar")
}

tasks.test {
    useJUnitPlatform()
}