package cl.esfocar.msapuntes;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApunteControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ApunteRepository repository;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    private String json(String titulo, String contenido) {
        return "{\"alias\":\"cadete_01\",\"titulo\":\"" + titulo + "\",\"contenido\":\"" + contenido + "\"}";
    }

    private Apunte guardar(String titulo, String contenido) {
        Apunte apunte = new Apunte();
        apunte.setAlias("cadete_01");
        apunte.setTitulo(titulo);
        apunte.setContenido(contenido);
        apunte.setFecha("01-01-2026 10:00");
        return repository.save(apunte);
    }

    @Test
    void crearYListarApuntes() throws Exception {
        mvc.perform(post("/api/apuntes").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Protocolo", "Revisar el protocolo antes de actuar")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titulo", is("Protocolo")));

        mvc.perform(get("/api/apuntes").param("alias", "cadete_01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(get("/api/apuntes").param("alias", "otro_usuario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void crearConDatosInvalidosDevuelve400() throws Exception {
        mvc.perform(post("/api/apuntes").contentType(MediaType.APPLICATION_JSON)
                        .content(json("ab", "corto")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void actualizarApunte() throws Exception {
        Apunte guardado = guardar("Original", "Contenido original largo");

        mvc.perform(put("/api/apuntes/" + guardado.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(json("Editado", "Contenido editado largo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo", is("Editado")));
    }

    @Test
    void eliminarApunte() throws Exception {
        Apunte guardado = guardar("Borrar", "Contenido para borrar");

        mvc.perform(delete("/api/apuntes/" + guardado.getId())).andExpect(status().isNoContent());
        mvc.perform(get("/api/apuntes/" + guardado.getId())).andExpect(status().isNotFound());
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/apuntes/9999")).andExpect(status().isNotFound());
    }
}
