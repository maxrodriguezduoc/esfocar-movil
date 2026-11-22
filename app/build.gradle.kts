import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    jacoco
}

// Datos de firma del APK (archivo keystore.properties en la raíz del proyecto)
val firma = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

// Dirección de los microservicios. 10.0.2.2 = el PC desde el emulador de Android.
// En un teléfono real: ./gradlew assembleRelease -PbackendHost=192.168.X.X (IP del PC en la red)
val backendHost = (project.findProperty("backendHost") as String?) ?: "10.0.2.2"

android {
    namespace = "com.example.esfocarapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.esfocarapp"
        minSdk = 33
        targetSdk = 37
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "URL_APUNTES", "\"http://$backendHost:8081/\"")
        buildConfigField("String", "URL_RESULTADOS", "\"http://$backendHost:8082/\"")
        buildConfigField("String", "URL_CONTENIDOS", "\"http://$backendHost:8083/\"")
        // Código ficticio para registrarse como docente (MVP académico; en producción lo entregaría la institución)
        buildConfigField("String", "CODIGO_DOCENTE", "\"DOCENTE-ESFOCAR\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file(firma.getProperty("storeFile", "esfocar-release.jks"))
            storePassword = firma.getProperty("storePassword")
            keyAlias = firma.getProperty("keyAlias")
            keyPassword = firma.getProperty("keyPassword")
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            // R8 minimiza y optimiza el código propio. No se amplía el alcance a kotlin.* / androidx.*
            // porque eso provocaba IllegalAccessError al abrir el APK release.
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.work:work-runtime-ktx:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.13.17")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
// ---------- Cobertura de pruebas unitarias (JaCoCo) ----------
// Ejecutar: ./gradlew jacocoReporte  ->  app/build/reports/jacoco/jacocoReporte/html/index.html
tasks.register<JacocoReport>("jacocoReporte") {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "Genera el reporte de cobertura de la lógica (ViewModels, repositorios, validaciones)"

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    // Se mide la lógica de la app. Se excluyen pantallas Compose, código generado y clases de Android puro.
    val excluidos = listOf(
        "**/ui/**", "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/*ComposableSingletons*",
        "**/*_Impl*", "**/MainActivity*", "**/EsfocarApp*", "**/AppContainer*",
        "**/data/local/AppDatabase*", "**/data/local/*Dao*", "**/data/remote/Apis*",
        "**/util/Notificaciones*", "**/util/Vibracion*", "**/viewmodel/Fabrica*",
        "**/util/MonitorConexion*", "**/util/PreferenciasAndroid*", "**/data/TrabajoSincronizacion*",
        "**/util/Documentos*", "**/util/DatosArchivo*",
        "**/data/remote/Dtos*", "**/model/**"
    )
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
            exclude(excluidos)
        }
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) { include("jacoco/testDebugUnitTest.exec", "outputs/unit_test_code_coverage/**/*.exec") }
    )
}
