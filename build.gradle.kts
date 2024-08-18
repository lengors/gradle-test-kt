import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.io.BufferedReader

plugins {
    kotlin("jvm")

    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.mooltiverse.oss.nyx")

    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

val (repositoryOwner, repositoryName) =
    Runtime
        .getRuntime()
        .exec(arrayOf("git", "config", "--get", "remote.origin.url"))
        .inputStream
        .bufferedReader()
        .use(BufferedReader::readText)
        .trim()
        .let {
            ".*[:/]([^/]+)/([^/.]+)(\\.git)?$"
                .toRegex()
                .find(it)
        }
        ?.destructured
        ?: throw UnknownRepositoryException("Failed to resolve git owner and name")

if (project.name != repositoryName) {
    throw InvalidUserDataException("Mismatch between repository name ($repositoryName) and project name (${project.name}")
}

group = "io.github.$repositoryOwner"

val junitVersion: String by properties
val javaVersion: String by properties
val mockitoVersion: String by properties

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
}

kotlin {
    jvmToolchain(javaVersion.toInt())
}

java {
    withJavadocJar()
    withSourcesJar()
}

nyx {
    configurationFile = ".nyx.yml"
}

tasks {
    clean {
        dependsOn(nyxClean)
    }

    test {
        useJUnitPlatform()
    }

    dokkaJavadoc {

        // Tells dokka javadoc plugin to depend on nyxInfer to generate
        // documentation forcing it to use the version inferred by nyx
        if (gradle.taskGraph.hasTask(nyxInfer.get())) {
            println("Task graph contains nyx infer")
            dependsOn(nyxInfer)
        }

        moduleName = project.name
    }

    named<Jar>("javadocJar") {
        dependsOn(dokkaJavadoc)
        from(dokkaJavadoc.flatMap { it.outputDirectory })
    }

    koverBinaryReport {
        dependsOn(test)
    }

    koverHtmlReport {
        dependsOn(test)
    }

    koverXmlReport {
        dependsOn(test)
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$repositoryOwner/$repositoryName")

            credentials {
                username = System.getenv("GITHUB_USERNAME") ?: repositoryOwner
                password = System.getenv("GITHUB_PASSWORD")
            }
        }
    }

    publications {
        register<MavenPublication>("GitHub") {
            from(components["java"])

            pom {
                val urlScm = "https://github.com/$repositoryOwner/$repositoryName"

                name = repositoryName
                description = "Test for gradle project using kotlin as its language"
                url = urlScm

                developers {
                    developer {
                        name = "lengors"
                        email = "24527258+lengors@users.noreply.github.com"
                    }
                }

                issueManagement {
                    system = "Github"
                    url = "$urlScm/issues"
                }

                scm {
                    connection = "$urlScm.git"
                    url = urlScm
                }

                licenses {
                    license {
                        name = "The Unlicense"
                        url = "https://unlicense.org/UNLICENSE"
                    }
                }
            }
        }
    }
}

signing {
    setRequired {
        !gradle.taskGraph.hasTask(
            tasks
                .named("publishToMavenLocal")
                .get(),
        )
    }

    val signingGnupgPassphrase = properties.get("signingGnupgPassphrase")
    signingGnupgPassphrase?.let {
        extraProperties.set("signing.gnupg.passphrase", it)
    }

    useGpgCmd()
    sign(publishing.publications)
}
