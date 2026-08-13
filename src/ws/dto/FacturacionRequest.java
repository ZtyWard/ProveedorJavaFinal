package ws.dto;

import java.time.LocalDate;

public class FacturacionRequest {

    private LocalDate fechaCalculo;
    private LocalDate fechaMaximaPago;

    public FacturacionRequest() {
    }

    public FacturacionRequest(
            LocalDate fechaCalculo,
            LocalDate fechaMaximaPago) {

        this.fechaCalculo = fechaCalculo;
        this.fechaMaximaPago = fechaMaximaPago;
    }

    public LocalDate getFechaCalculo() {
        return fechaCalculo;
    }

    public void setFechaCalculo(LocalDate fechaCalculo) {
        this.fechaCalculo = fechaCalculo;
    }

    public LocalDate getFechaMaximaPago() {
        return fechaMaximaPago;
    }

    public void setFechaMaximaPago(LocalDate fechaMaximaPago) {
        this.fechaMaximaPago = fechaMaximaPago;
    }
}
