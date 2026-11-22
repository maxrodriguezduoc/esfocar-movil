package com.example.esfocarapp.data

import com.example.esfocarapp.data.local.ApunteDao
import com.example.esfocarapp.data.local.ApunteLocal
import com.example.esfocarapp.data.local.EstadoSync
import com.example.esfocarapp.data.remote.ApunteRemoto
import com.example.esfocarapp.data.remote.ApunteSolicitud
import com.example.esfocarapp.data.remote.ApuntesApi
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.data.remote.llamarApi
import kotlinx.coroutines.flow.Flow

// Apuntes OFFLINE-FIRST:
//  - Crear, editar y borrar ocurre al instante en Room y el cambio queda marcado como "pendiente".
//  - sincronizar() sube los pendientes a ms-apuntes y luego trae los cambios del servidor.
class ApuntesRepository(
    private val dao: ApunteDao,
    private val api: ApuntesApi,
    private val fecha: () -> String = AppRepository::fechaPorDefecto,
    private val alCambiar: () -> Unit = {}
) {

    fun apuntes(alias: String): Flow<List<ApunteLocal>> = dao.apuntes(alias)

    suspend fun crear(alias: String, titulo: String, contenido: String) {
        dao.insertar(ApunteLocal(alias = alias, titulo = titulo, contenido = contenido, fecha = fecha()))
        alCambiar()
    }

    suspend fun actualizar(apunte: ApunteLocal, titulo: String, contenido: String) {
        val estado = if (apunte.idRemoto == null) EstadoSync.CREAR else EstadoSync.ACTUALIZAR
        dao.actualizar(apunte.copy(titulo = titulo, contenido = contenido, fecha = fecha(), estado = estado))
        alCambiar()
    }

    suspend fun eliminar(apunte: ApunteLocal) {
        if (apunte.idRemoto == null) {
            dao.eliminar(apunte)              // nunca llegó al servidor: se borra y listo
        } else {
            dao.actualizar(apunte.copy(estado = EstadoSync.ELIMINAR))
            alCambiar()
        }
    }

    // Devuelve cuántos cambios pendientes se subieron
    suspend fun sincronizar(alias: String?): Respuesta<Int> {
        val subida = subirPendientes()
        if (subida is Respuesta.Fallo) return subida
        if (alias != null) {
            when (val r = llamarApi { api.listar(alias) }) {
                is Respuesta.Fallo -> return r
                is Respuesta.Exito -> fusionar(alias, r.datos)
            }
        }
        return subida
    }

    suspend fun subirPendientes(): Respuesta<Int> {
        var subidos = 0
        for (a in dao.pendientes()) {
            val r = when (a.estado) {
                EstadoSync.CREAR -> subirNuevo(a)
                EstadoSync.ACTUALIZAR -> {
                    val res = llamarApi { api.actualizar(a.idRemoto!!, solicitud(a)) }
                    when {
                        res is Respuesta.Exito -> {
                            dao.actualizar(a.copy(estado = EstadoSync.SINCRONIZADO, fecha = res.datos.fecha))
                            res
                        }
                        // Lo borraron en el servidor: se vuelve a crear para no perder la edición
                        res is Respuesta.Fallo && res.codigo == 404 -> subirNuevo(a)
                        else -> res
                    }
                }
                else -> {   // ELIMINAR
                    val res = llamarApi { api.eliminar(a.idRemoto!!) }
                    if (res is Respuesta.Exito || (res is Respuesta.Fallo && res.codigo == 404)) {
                        dao.eliminar(a)
                        Respuesta.Exito(Unit)
                    } else {
                        res
                    }
                }
            }
            if (r is Respuesta.Fallo) return r
            subidos++
        }
        return Respuesta.Exito(subidos)
    }

    private suspend fun subirNuevo(a: ApunteLocal): Respuesta<ApunteRemoto> {
        val res = llamarApi { api.crear(solicitud(a)) }
        if (res is Respuesta.Exito) {
            dao.actualizar(a.copy(idRemoto = res.datos.id, estado = EstadoSync.SINCRONIZADO, fecha = res.datos.fecha))
        }
        return res
    }

    // Trae lo que hay en el servidor: agrega lo nuevo, actualiza lo sincronizado y quita lo borrado remotamente
    private suspend fun fusionar(alias: String, remotos: List<ApunteRemoto>) {
        val locales = dao.todosDe(alias)
        val porIdRemoto = locales.filter { it.idRemoto != null }.associateBy { it.idRemoto }
        val idsRemotos = remotos.map { it.id }.toSet()

        for (r in remotos) {
            val local = porIdRemoto[r.id]
            if (local == null) {
                dao.insertar(
                    ApunteLocal(
                        idRemoto = r.id, alias = alias, titulo = r.titulo, contenido = r.contenido,
                        fecha = r.fecha, estado = EstadoSync.SINCRONIZADO
                    )
                )
            } else if (local.estado == EstadoSync.SINCRONIZADO && (local.titulo != r.titulo || local.contenido != r.contenido)) {
                dao.actualizar(local.copy(titulo = r.titulo, contenido = r.contenido, fecha = r.fecha))
            }
        }
        locales.filter { it.idRemoto != null && it.estado == EstadoSync.SINCRONIZADO && it.idRemoto !in idsRemotos }
            .forEach { dao.eliminar(it) }
    }

    private fun solicitud(a: ApunteLocal) = ApunteSolicitud(a.alias, a.titulo, a.contenido)
}
