package modelo;

import java.sql.Timestamp;

public class Caja {

    private int idCaja;
    private int idUsuario;
    private Timestamp fechaApertura;
    private double montoInicial;
    private Timestamp fechaCierre;
    private double montoCierreReal;
    private double totalEfectivo;
    private double totalBilletera;
    private String estado;
    private String observacion;

    // Constructor vacío
    public Caja() {
    }

    // Constructor para abrir una caja
    public Caja(int idUsuario, double montoInicial) {
        this.idUsuario = idUsuario;
        this.montoInicial = montoInicial;
        this.estado = "Abierta";
    }

    // GETTERS Y SETTERS

    public int getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(int idCaja) {
        this.idCaja = idCaja;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Timestamp getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(Timestamp fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public double getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(double montoInicial) {
        this.montoInicial = montoInicial;
    }

    public Timestamp getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(Timestamp fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public double getMontoCierreReal() {
        return montoCierreReal;
    }

    public void setMontoCierreReal(double montoCierreReal) {
        this.montoCierreReal = montoCierreReal;
    }

    public double getTotalEfectivo() {
        return totalEfectivo;
    }

    public void setTotalEfectivo(double totalEfectivo) {
        this.totalEfectivo = totalEfectivo;
    }

    public double getTotalBilletera() {
        return totalBilletera;
    }

    public void setTotalBilletera(double totalBilletera) {
        this.totalBilletera = totalBilletera;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}