package compensar;

import compensar.service.CalculadoraService;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        CalculadoraService service = new CalculadoraService();

        System.out.println("Suma: " + service.sumar(2, 3));
        System.out.println("Resta: " + service.restar(5, 2));
        System.out.println("División: " + service.dividir(10, 2));
    }
}