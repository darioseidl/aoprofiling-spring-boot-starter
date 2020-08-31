plugins {
    val kotlinVersion = "1.4.0"
    val springBootVersion = "2.3.3.RELEASE"
    `java-library`
    id("org.springframework.boot") version springBootVersion apply false
    id("io.spring.dependency-management") version "1.0.9.RELEASE" apply false
    kotlin("jvm") version kotlinVersion apply false
    kotlin("plugin.spring") version kotlinVersion apply false
    id("idea")
    id("net.researchgate.release") version "2.6.0"
    maven
    `maven-publish`
}

group = "at.darioseidl.aoprofiling"

fun Project.envConfig() = object : kotlin.properties.ReadOnlyProperty<Any?, String?> {
    override fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): String? =
            if (extensions.extraProperties.has(property.name)) {
                extensions.extraProperties[property.name] as? String
            } else {
                System.getenv(property.name)
            }
}

release {
    buildTasks = listOf("releaseBuild")
}

tasks.register("releaseBuild") {
    dependsOn(subprojects.map { it.tasks.findByName("build") })
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    group = "at.darioseidl.aoprofiling"

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            suppressWarnings = true
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks {
        val sourcesJar by creating(Jar::class) {
            dependsOn(JavaPlugin.CLASSES_TASK_NAME)
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        val javadocJar by creating(Jar::class) {
            dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
            archiveClassifier.set("javadoc")
            from(javadoc)
        }

        artifacts {
            archives(sourcesJar)
            archives(javadocJar)
            archives(jar)
        }
    }

    tasks.withType<Javadoc> {
        if (JavaVersion.current().isJava8Compatible) {
            (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
        }
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
    }

    val repositoryUser by envConfig()
    val repositoryPassword by envConfig()
    publishing {
        repositories {
            maven {
                name = "MavenCentral"
                val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
                val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
                url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                credentials {
                    username = repositoryUser
                    password = repositoryPassword
                }
            }
        }
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])

                pom {
                    name.set("Sample Spring Boot Starter")
                    description.set("A Spring Boot Starter example.")
                    url.set("https://<Website or Repository URL>")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("<Developer Id>")
                            name.set("<Developer Name>")
                            email.set("<Developer Email>")
                        }
                    }
                    scm {
                        connection.set("https://<Repository URL>.git")
                        developerConnection.set("https://<Repository URL>.git")
                        url.set("https://<Website URL>")
                    }
                }
            }
        }
    }
}

tasks.named("afterReleaseBuild") {
    dependsOn(listOf(
            ":aoprofiling-autoconfigure:publish",
            ":aoprofiling-starter:publish",
            ":aoprofiling:publish"
    ))
}