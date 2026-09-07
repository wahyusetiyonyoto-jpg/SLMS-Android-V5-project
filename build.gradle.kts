plugins {
    id("com.android.application") version "8.7.3"
}

val generatedAssetsDir = layout.buildDirectory.dir("generated/qie/assets")
val generatedResDir = layout.buildDirectory.dir("generated/qie/res")
val generatedJavaDir = layout.buildDirectory.dir("generated/qie/java")

val prepareFlatSources = tasks.register("prepareFlatSources") {
    outputs.dir(generatedAssetsDir)
    outputs.dir(generatedResDir)
    outputs.dir(generatedJavaDir)

    doLast {
        require(file("jsQR.js").isFile) {
            "jsQR.js is missing. Run scripts/fetch-jsqr.sh before building."
        }
        require(file("qie-qrcode-local.js").isFile) {
            "qie-qrcode-local.js is missing from the project."
        }

        delete(generatedAssetsDir.get().asFile)
        delete(generatedResDir.get().asFile)
        delete(generatedJavaDir.get().asFile)

        copy {
            from(projectDir)
            include(
                "index.html.gz.b64.*",
                "initial-state.js.gz.b64.*",
                "bundled-qr.js.gz.b64.*",
                "logo.webp.b64.*",
                "jsQR.js",
                "qie-qrcode-local.js"
            )
            into(generatedAssetsDir.get().asFile)
        }

        copy {
            from(projectDir)
            include("strings.xml", "colors.xml", "themes.xml")
            into(generatedResDir.get().dir("values").asFile)
        }

        copy {
            from(projectDir)
            include("ic_launcher.xml", "launcher_logo.webp")
            into(generatedResDir.get().dir("drawable").asFile)
        }

        copy {
            from(projectDir)
            include("file_paths.xml")
            into(generatedResDir.get().dir("xml").asFile)
        }

        copy {
            from(projectDir)
            include("MainActivity.java")
            into(generatedJavaDir.get().asFile)
        }
    }
}

val releaseStorePath = System.getenv("QIE_KEYSTORE_PATH")
val releaseStorePassword = System.getenv("QIE_KEYSTORE_PASSWORD")
val releaseKeyAlias = System.getenv("QIE_KEY_ALIAS")
val releaseKeyPassword = System.getenv("QIE_KEY_PASSWORD")
val hasReleaseSigning = listOf(
    releaseStorePath,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { !it.isNullOrBlank() }

android {
    namespace = "com.wahyusetiyonyoto.quickidentifyequipment"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.wahyusetiyonyoto.quickidentifyequipment"
        minSdk = 24
        targetSdk = 35
        versionCode = 10
        versionName = "5.4.1"
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.setSrcDirs(listOf(generatedJavaDir.get().asFile))
            assets.setSrcDirs(listOf(generatedAssetsDir.get().asFile))
            res.setSrcDirs(listOf(generatedResDir.get().asFile))
        }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStorePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.named("preBuild").configure {
    dependsOn(prepareFlatSources)
}

dependencies {
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.webkit:webkit:1.12.1")
}
