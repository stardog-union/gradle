plugins {
    java
    `maven-publish`
    signing
}

group = "gradle"

// tag::conditional-signing[]
version.set("1.0-SNAPSHOT")
extra["isReleaseVersion"] = !version.get().endsWith("SNAPSHOT")

// end::conditional-signing[]
publishing {
    publications {
        create<MavenPublication>("main") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "localRepo"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

// tag::conditional-signing[]
signing {
    setRequired({
        (project.extra["isReleaseVersion"] as Boolean) && gradle.taskGraph.hasTask("publish")
    })
    sign(publishing.publications["main"])
}
// end::conditional-signing[]

// Alternative to signing.required
// tag::only-if[]
tasks.withType<Sign>().configureEach {
    onlyIf { project.extra["isReleaseVersion"] as Boolean }
}
// end::only-if[]
