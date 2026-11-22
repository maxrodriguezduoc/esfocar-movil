package cl.esfocar.mscontenidos;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ContenidoControllerTest {

    @Autowired MockMvc mvc;

    private static final String PREGUNTA = """
            {"enunciado":"¿Pregunta nueva?","explicacion":"Porque sí",
             "alternativas":[
               {"texto":"A","correcta":true,"retroalimentacion":"Bien"},
               {"texto":"B","correcta":false,"retroalimentacion":"Mal"}]}
            """;

    private static final String CASO = """
            {"titulo":"Caso nuevo","situacion":"Una situación","pregunta":"¿Qué haces?","dificultad":"MEDIA",
             "fundamento":"Fundamento",
             "opciones":[
               {"texto":"Actuar bien","puntaje":2,"retroalimentacion":"Óptimo"},
               {"texto":"Actuar mal","puntaje":0,"retroalimentacion":"Incorrecto"}]}
            """;

    @Test
    void laSemillaSeCargaAlIniciar() throws Exception {
        mvc.perform(get("/api/contenidos/version"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version", is(1)));

        mvc.perform(get("/api/contenidos/paquete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.asignaturas", hasSize(4)))
                .andExpect(jsonPath("$.asignaturas[0].unidades[0].fichas[0].puntosClave", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.asignaturas[0].unidades[0].preguntas[0].alternativas", hasSize(greaterThan(1))))
                .andExpect(jsonPath("$.asignaturas[0].unidades[0].casos[0].opciones", hasSize(3)));
    }

    @Test
    void obtenerAsignaturaYNoExistente() throws Exception {
        mvc.perform(get("/api/asignaturas/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Primeros Auxilios")));
        mvc.perform(get("/api/asignaturas/99")).andExpect(status().isNotFound());
    }

    @Test
    void crearEditarYEliminarPreguntaSubeLaVersion() throws Exception {
        mvc.perform(post("/api/unidades/11/preguntas").contentType(MediaType.APPLICATION_JSON).content(PREGUNTA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(4206)))
                .andExpect(jsonPath("$.alternativas", hasSize(2)));

        mvc.perform(put("/api/preguntas/4206").contentType(MediaType.APPLICATION_JSON)
                        .content(PREGUNTA.replace("¿Pregunta nueva?", "¿Editada?")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enunciado", is("¿Editada?")));

        mvc.perform(delete("/api/preguntas/4206")).andExpect(status().isNoContent());
        mvc.perform(delete("/api/preguntas/4206")).andExpect(status().isNotFound());

        mvc.perform(get("/api/contenidos/version")).andExpect(jsonPath("$.version", is(4)));
    }

    @Test
    void preguntaSinUnaCorrectaEsRechazada() throws Exception {
        mvc.perform(post("/api/unidades/11/preguntas").contentType(MediaType.APPLICATION_JSON)
                        .content(PREGUNTA.replace("\"correcta\":true", "\"correcta\":false")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("La pregunta debe tener exactamente una alternativa correcta")));
    }

    @Test
    void preguntaConUnaSolaAlternativaEsRechazada() throws Exception {
        String una = """
                {"enunciado":"x","alternativas":[{"texto":"A","correcta":true,"retroalimentacion":"Bien"}]}
                """;
        mvc.perform(post("/api/unidades/11/preguntas").contentType(MediaType.APPLICATION_JSON).content(una))
                .andExpect(status().isBadRequest());
    }

    @Test
    void preguntaEnUnidadInexistenteDevuelve404() throws Exception {
        mvc.perform(post("/api/unidades/999/preguntas").contentType(MediaType.APPLICATION_JSON).content(PREGUNTA))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearEditarYEliminarCaso() throws Exception {
        mvc.perform(post("/api/unidades/31/casos").contentType(MediaType.APPLICATION_JSON).content(CASO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(4253)))
                .andExpect(jsonPath("$.opciones", hasSize(2)));

        mvc.perform(put("/api/casos/4253").contentType(MediaType.APPLICATION_JSON)
                        .content(CASO.replace("Caso nuevo", "Caso editado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo", is("Caso editado")));

        mvc.perform(delete("/api/casos/4253")).andExpect(status().isNoContent());
        mvc.perform(put("/api/casos/4253").contentType(MediaType.APPLICATION_JSON).content(CASO))
                .andExpect(status().isNotFound());
    }

    @Test
    void casoSinActuacionOptimaODificultadInvalidaEsRechazado() throws Exception {
        mvc.perform(post("/api/unidades/31/casos").contentType(MediaType.APPLICATION_JSON)
                        .content(CASO.replace("\"puntaje\":2", "\"puntaje\":1")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/unidades/31/casos").contentType(MediaType.APPLICATION_JSON)
                        .content(CASO.replace("MEDIA", "EXTREMA")))
                .andExpect(status().isBadRequest());
    }
}
