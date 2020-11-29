import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val developmentOnly: Configuration by configurations.creating
val micronautVersion: String by project
val micronautDataVersion: String by project
val micronautTestVersion: String by project
val kotlinVersion: String by project

plugins {
    val kotlinVersion = "1.3.50"
    application
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("com.palantir.docker") version "0.25.0"
    id("nebula.rpm") version "8.4.1"
}

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.netflix.nebula:gradle-ospackage-plugin:8.4.1")
    }
}

repositories {
    mavenCentral()
    maven("https://jcenter.bintray.com")
    maven("https://www.jitpack.io")
}

configurations {
    // for dependencies that are needed for development only
    developmentOnly
}

docker {
    name = "hub.docker.com/wuvist/blogapi:1.0"
    copySpec.from("build/libs").into("build/libs")
}

dependencies {
    implementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    implementation(kotlin("stdlib:$kotlinVersion"))
    implementation(kotlin("reflect:$kotlinVersion"))
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-management")
    implementation("jakarta.persistence:jakarta.persistence-api:2.2.2")
    implementation("io.micronaut.data:micronaut-data-jdbc:$micronautDataVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx2:1.3.7")
    kapt(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kapt("io.micronaut:micronaut-inject-java")
    kapt("io.micronaut:micronaut-validation")
    kapt("io.micronaut.data:micronaut-data-processor:$micronautDataVersion")
    kaptTest(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kaptTest("io.micronaut:micronaut-inject-java")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
    runtimeOnly("com.h2database:h2")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    testImplementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    testImplementation("io.micronaut.test:micronaut-test-kotest:$micronautTestVersion")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.0.3")
    testImplementation("com.github.Wuvist:easywebmock:master-SNAPSHOT")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.3.1")
}

application {
    version = "0.1"
    group = "blogwind.com"
    mainClassName = "blogwind.com.Application"
}

allOpen {
    annotation("io.micronaut.aop.Around")
}

tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
    }

    register("fooRpm", com.netflix.gradle.plugins.rpm.Rpm::class) {
        release = "1"

        from(project.tasks["shadowJar"].outputs.files) {
            into("lib")
        }

        from("src/main/resources") {
            into("conf")
        }
    }

    withType<Test>{
        classpath = classpath.plus(configurations["developmentOnly"])
        useJUnitPlatform()
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            javaParameters = true
        }
    }

    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            javaParameters = true
        }
    }

    named<JavaExec>("run") {
        doFirst {
            classpath = classpath.plus(configurations["developmentOnly"])
            jvmArgs = listOf("-noverify", "-XX:TieredStopAtLevel=1", "-Dcom.sun.management.jmxremote")
        }
    }
}
val test by tasks.getting(Test::class) {
    // ... Other configurations ...
    systemProperties = System.getProperties().map { it.key.toString() to it.value }.toMap()
}