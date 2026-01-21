import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.gradleup.shadow") version "8.3.0"
  id("io.ebean") version "13.11.0"
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.5.1"
val junitJupiterVersion = "5.9.1"

application {
  mainClass.set("io.vertx.core.Launcher")
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))

  implementation("io.vertx:vertx-core")
  implementation("io.vertx:vertx-web")

  implementation("io.vertx:vertx-rx-java3")

  implementation("io.vertx:vertx-auth-jwt")

  implementation("io.ebean:ebean:13.11.0")
  implementation("io.ebean:ebean-querybean:13.11.0")
  implementation("mysql:mysql-connector-j:8.3.0")

  implementation("com.opencsv:opencsv:5.9")
  implementation("ch.qos.logback:logback-classic:1.4.14")

  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")

  implementation("io.vertx:vertx-auth-jwt:4.5.1")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to "com.example.starter.MainVerticle"))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", "com.example.starter.MainVerticle")
}
