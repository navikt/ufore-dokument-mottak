plugins {
    kotlin("jvm") version "2.4.21"
    kotlin("plugin.serialization") version "2.4.21"
    application
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
}

val ktorVersjon = "3.6.1"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testImplementation("io.mockk:mockk:1.14.12")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersjon")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.kotest:kotest-assertions-core-jvm:6.2.6")

    implementation("io.github.oshai:kotlin-logging-jvm:8.0.6")
    implementation("org.apache.kafka:kafka-clients:8.4.0-ccs")
    implementation("org.apache.avro:avro:1.12.2")
    implementation("io.confluent:kafka-avro-serializer:8.4.0")
    implementation("io.ktor:ktor-server-core:$ktorVersjon")
    implementation("io.ktor:ktor-server-netty:$ktorVersjon")
    implementation("io.ktor:ktor-client-core:$ktorVersjon")
    implementation("io.ktor:ktor-client-cio:$ktorVersjon")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersjon")
    implementation("io.micrometer:micrometer-registry-prometheus:1.17.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")
    runtimeOnly("ch.qos.logback:logback-classic:1.6.6")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:9.0")
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
    environment("NAIS_TOKEN_ENDPOINT", "http://localhost/token")
    environment("SAF_SCOPE", "api://localhost/.default")
}
