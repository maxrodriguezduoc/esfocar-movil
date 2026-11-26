package com.example.esfocarapp.util

import android.content.Context
import androidx.core.content.edit

enum class Tema { SISTEMA, CLARO, OSCURO }

// Ajustes pequeños que deben sobrevivir al cerrar la app
interface Preferencias {
    var onboardingVisto: Boolean
    var aliasSesion: String?
    var rolSesion: String
    var versionContenido: Int
    var tema: Tema
}

class PreferenciasAndroid(context: Context) : Preferencias {

    private val sp = context.getSharedPreferences("esfocar_prefs", Context.MODE_PRIVATE)

    override var onboardingVisto: Boolean
        get() = sp.getBoolean("onboarding", false)
        set(valor) = sp.edit { putBoolean("onboarding", valor) }

    override var aliasSesion: String?
        get() = sp.getString("alias", null)
        set(valor) = sp.edit { if (valor == null) remove("alias") else putString("alias", valor) }

    override var rolSesion: String
        get() = sp.getString("rol", Rol.ESTUDIANTE) ?: Rol.ESTUDIANTE
        set(valor) = sp.edit { putString("rol", valor) }

    override var versionContenido: Int
        get() = sp.getInt("version_contenido", 0)
        set(valor) = sp.edit { putInt("version_contenido", valor) }

    override var tema: Tema
        get() = runCatching { Tema.valueOf(sp.getString("tema", Tema.SISTEMA.name)!!) }.getOrDefault(Tema.SISTEMA)
        set(valor) = sp.edit { putString("tema", valor.name) }
}
