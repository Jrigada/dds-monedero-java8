package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {

  private double saldo = 0;
  private List<Movimiento> movimientos = new ArrayList<>();
  private double limite = 1000;

  public Cuenta() {
    saldo = 0;
  }

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public boolean cantidadDeMovimientosEnElDiaMayorA(int numeroMovimientos) {
    return getMovimientos()
        .stream()
        .filter(movimiento -> movimiento.isDeposito() && movimiento.esDeLaFecha(LocalDate.now()))
        .count()>=numeroMovimientos;
  }

  public void montoMayorACero(double monto) {
    if (monto <= 0) {
      throw new MontoNegativoException(monto + ": el monto a ingresar debe ser un valor positivo");
    }
  }

  public void poner(double cuanto) {

    montoMayorACero(cuanto);

    if (cantidadDeMovimientosEnElDiaMayorA(3)) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }

    agregarMovimiento(LocalDate.now(),cuanto,true);
  }

  public void excedeLimiteDeSaldo(double cantidad) {
    if (getSaldo() - cantidad < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
  }

  public void sacar(double cuanto) {

    montoMayorACero(cuanto);

    excedeLimiteDeSaldo(cuanto);

    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    limite = 1000 - montoExtraidoHoy;

    excedeLimiteDeCuenta(cuanto);

    agregarMovimiento(LocalDate.now(),cuanto,false);
  }

  public void excedeLimiteDeCuenta(double cantidad){
    if (cantidad > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + limite
          + " diarios, límite: " + limite);
    }
  }

  public void agregarMovimiento(LocalDate fecha, double cuanto, boolean esDeposito) {
    Movimiento movimiento = new Movimiento(fecha, cuanto, esDeposito);
    movimientos.add(movimiento);
    double valorMovimiento = movimiento.getMonto();
    setSaldo(saldo + valorMovimiento);
    }

  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> !movimiento.isDeposito() && movimiento.esDeLaFecha(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

}
