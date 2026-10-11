plugins {
    id("java")
}

group = "org.sample"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")

    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")

    // The ByteChef server provides these at runtime, so they are compile-only and not packed into the jar.
    compileOnly(files("libs/integration-api-1.0.jar", "libs/workflow-api-1.0.jar"))
}

tasks.jar {
    archiveBaseName.set("sample-code-integration-java")
}
