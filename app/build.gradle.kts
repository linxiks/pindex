plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Offline database produced by tools/data-builder; packaged as an asset and read by JVM tests.
val pokedexDb = rootProject.layout.projectDirectory.file("data/generated/pokedex.db")
val pokedexAssets = layout.buildDirectory.dir("generated/pokedexAssets")

android {
    namespace = "io.github.linxiks.pindex"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.linxiks.pindex"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    sourceSets["main"].assets.srcDir(pokedexAssets)

    testOptions {
        unitTests.all {
            it.systemProperty("pokedex.db", pokedexDb.asFile.absolutePath)
        }
    }
}

// Separate task: Sync skips its actions (including doFirst) when the source file is missing.
val checkPokedexDb = tasks.register("checkPokedexDb") {
    val db = pokedexDb.asFile
    doLast {
        if (!db.exists()) throw GradleException(
            "data/generated/pokedex.db 不存在：先在仓库根目录运行 python3 tools/data-builder/fetch.py 和 python3 tools/data-builder/build.py"
        )
    }
}
val syncPokedexDb = tasks.register<Sync>("syncPokedexDb") {
    dependsOn(checkPokedexDb)
    from(pokedexDb)
    into(pokedexAssets)
}
tasks.named("preBuild") { dependsOn(syncPokedexDb) }

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.sqlite.jdbc)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
