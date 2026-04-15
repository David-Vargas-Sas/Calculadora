package compensar.service;

import compensar.model.Calculadora;

public class CalculadoraService {

    private Calculadora calculadora;

    public CalculadoraService() {
        this.calculadora = new Calculadora();
    }

    public int sumar(int a, int b) {
        return calculadora.sumar(a, b);
    }

    public int restar(int a, int b) {
        return calculadora.restar(a, b);
    }

    public int dividir(int a, int b) {
        return calculadora.dividir(a, b);
    }
}