package cl.esfocar.msresultados;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class ResultadoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ResultadoRepository repository;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    private String json(int aciertos, int total) {
        return "{\"alias\":\"cadete_01\",\"actividad\":\"Trivia rapida\",\"aciertos\":" + aciertos
                + ",\"total\":" + total + "}";
    }

    @Test
    void crearYListarResultados() throws Exception {
        mvc.perform(post("/api/resultados").contentType(MediaType.APPLICATION_JSON).content(json(2, 3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aciertos", is(2)));

        mvc.perform(get("/api/resultados").param("alias", "cadete_01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void aciertosMayoresQueTotalDevuelve400() throws Exception {
        mvc.perform(post("/api/resultados").contentType(MediaType.APPLICATION_JSON).content(json(5, 3)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void totalCeroDevuelve400() throws Exception {
        mvc.perform(post("/api/resultados").contentType(MediaType.APPLICATION_JSON).content(json(0, 0)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resumenCalculaPromedioYMejor() throws Exception {
        mvc.perform(post("/api/resultados").contentType(MediaType.APPLICATION_JSON).content(json(3, 3)));
        mvc.perform(post("/api/resultados").contentType(MediaType.APPLICATION_JSON).content(json(1, 2)));

        mvc.perform(get("/api/resultados/resumen").param("alias", "cadete_01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intentos", is(2)))
                .andExpect(jsonPath("$.promedioPorcentaje", is(75.0)))
                .andExpect(jsonPath("$.mejorPorcentaje", is(100)));
    }

    @Test
    void resumenSinDatosEsCero() throws Exception {
        mvc.perform(get("/api/resultados/resumen").param("alias", "nadie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intentos", is(0)));
    }

    @Test
    void eliminarResultado() throws Exception {
        Resultado r = new Resultado();
        r.setAlias("cadete_01");
        r.setActividad("Trivia");
        r.setAciertos(1);
        r.setTotal(2);
        r.setFecha("01-01-2026 10:00");
        r = repository.save(r);

        mvc.perform(delete("/api/resultados/" + r.getId())).andExpect(status().isNoContent());
        mvc.perform(delete("/api/resultados/" + r.getId())).andExpect(status().isNotFound());
    }
}
