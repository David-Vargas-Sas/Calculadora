
import compensar.service.CalculadoraService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("🧪 Pruebas del servicio de Calculadora")
public class CalculadoraServiceTest {

    static CalculadoraService service;

    @BeforeAll
    static void antesDeTodo() {
        System.out.println("Iniciando todas las pruebas...");
        service = new CalculadoraService();
    }

    @AfterAll
    static void despuesDeTodo() {
        System.out.println("Finalizaron todas las pruebas");
    }

    @BeforeEach
    void antesDeCadaPrueba() {
        System.out.println("Iniciando prueba...");
    }

    @AfterEach
    void despuesDeCadaPrueba() {
        System.out.println("Prueba finalizada");
    }

    @Test
    @DisplayName("Suma correcta")
    void testSumar() {
        assertEquals(5, service.sumar(2, 3));
    }

    @Test
    @DisplayName("Resta correcta")
    void testRestar() {
        assertEquals(3, service.restar(5, 2));
    }

    @Test
    @DisplayName("División correcta")
    void testDividir() {
        assertEquals(2, service.dividir(4, 2));
    }

    @Test
    @DisplayName("División por cero (excepción)")
    void testDivisionError() {
        assertThrows(ArithmeticException.class, () -> {
            service.dividir(4, 0);
        });
    }

    @Test
    @Disabled("Prueba deshabilitada temporalmente")
    @DisplayName("Prueba deshabilitada")
    void testDeshabilitado() {
        fail("Esta prueba no debe ejecutarse");
    }
}