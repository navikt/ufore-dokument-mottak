plugins {
    alias(libs.plugins.kotlin.jvm)

    application
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.kotlin.logging.jvm)
    implementation(libs.kafka.clients)
    implementation(libs.avro)
    implementation(libs.confluent.avro.serializer)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.micrometer.prometheus)
    implementation(libs.kotlinx.serialization)
    runtimeOnly(libs.logback.classic)
    runtimeOnly(libs.logstash.logback)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "no.nav.uføre.AppKt"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "no.nav.uføre.AppKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.map { cp ->
        cp.map { if (it.isDirectory) it else zipTree(it) }
    })
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
