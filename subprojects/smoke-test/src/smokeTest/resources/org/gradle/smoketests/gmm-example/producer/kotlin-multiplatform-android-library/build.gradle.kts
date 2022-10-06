import com.android.build.api.dsl.ApplicationBaseFlavor
import com.android.build.api.dsl.ApplicationVariantDimension

plugins {
    id("com.android.library")
    kotlin("multiplatform")
}

android {
    compileSdk = 30
    buildToolsVersion = "30.0.2"
    defaultConfig {
<<<<<<< HEAD
        minSdkVersion(16)
        targetSdkVersion(30)
=======
        minSdk = 16
        targetSdk = 30
        (this as ApplicationBaseFlavor).versionCode = 1
        (this as ApplicationBaseFlavor).versionName = "1.0"
>>>>>>> master
    }
    flavorDimensions.add("org.gradle.example.my-own-flavor")
    productFlavors {
        create("demo") {
<<<<<<< HEAD
            setDimension("org.gradle.example.my-own-flavor")
        }
        create("full") {
            setDimension("org.gradle.example.my-own-flavor")
=======
            dimension = "org.gradle.example.my-own-flavor"
            (this as ApplicationVariantDimension).versionNameSuffix = "-demo"
        }
        create("full") {
            dimension = "org.gradle.example.my-own-flavor"
            (this as ApplicationVariantDimension).versionNameSuffix = "-full"
>>>>>>> master
        }
    }

}

kotlin {
    // jvm()
    js()
    macosX64()
    linuxX64()
    android {
        publishLibraryVariants("fullRelease", "demoRelease",
                "fullDebug", "demoDebug")
    }
}

dependencies {
    "commonMainImplementation"(kotlin("stdlib-common"))
    // "jvmMainImplementation"(kotlin("stdlib"))
    "androidMainImplementation"(kotlin("stdlib"))
    "jsMainImplementation"(kotlin("stdlib-js"))
}

afterEvaluate {
    publishing {
        publications.forEach { println("Koltin-Native publication: ${it.name}") }
    }
}
