package weatherreport_mvc.models;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entidad que representa un registro meteorológico capturado en el sistema.
 * Humedad en %, velocidad del viento y lluvia en mm/h. La fecha y la hora se
 * manejan por separado, tal como las guarda la estación meteorológica.
 */
public class RegistroMeteorologico {

    private int id;
    private LocalDate fecha;
    private LocalTime hora;
    private double tempExterior;
    private double hiTemp;
    private double lowTemp;
    private double puntoRocio;
    private double sensacionTermica;
    private double indiceCalor;
    private double indiceThw;
    private double humedad;
    private double velocidadViento;
    private String direccionViento;
    private double recorridoViento;
    private double rafagaMaxima;
    private String direccionRafaga;
    private double presionBarometrica;
    private double lluvia;
    private double tasaLluvia;
    private double gradosDiaCalefaccion;
    private double gradosDiaRefrigeracion;
    private double tempInterior;
    private double humedadInterior;
    private double puntoRocioInterior;
    private double indiceCalorInterior;
    private double emcInterior;
    private double densidadAireInterior;
    private int muestrasViento;
    private int estadoTransmision;
    private int intervaloArchivo;
    private double calidadRecepcion;
    private String observaciones;

    /** Las mediciones empiezan como faltantes (NaN) hasta que se asigna un valor real. */
    public RegistroMeteorologico() {
        this.id = 0;
        this.tempExterior = Double.NaN;
        this.hiTemp = Double.NaN;
        this.lowTemp = Double.NaN;
        this.puntoRocio = Double.NaN;
        this.sensacionTermica = Double.NaN;
        this.indiceCalor = Double.NaN;
        this.indiceThw = Double.NaN;
        this.humedad = Double.NaN;
        this.velocidadViento = Double.NaN;
        this.recorridoViento = Double.NaN;
        this.rafagaMaxima = Double.NaN;
        this.presionBarometrica = Double.NaN;
        this.lluvia = Double.NaN;
        this.tasaLluvia = Double.NaN;
        this.gradosDiaCalefaccion = Double.NaN;
        this.gradosDiaRefrigeracion = Double.NaN;
        this.tempInterior = Double.NaN;
        this.humedadInterior = Double.NaN;
        this.puntoRocioInterior = Double.NaN;
        this.indiceCalorInterior = Double.NaN;
        this.emcInterior = Double.NaN;
        this.densidadAireInterior = Double.NaN;
        this.calidadRecepcion = Double.NaN;
        this.observaciones = "";
        this.direccionViento = "";
        this.direccionRafaga = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public double getHumedad() {
        return humedad;
    }

    public double getTempExterior() {
        return tempExterior;
    }

    public void setTempExterior(double tempExterior) {
        this.tempExterior = tempExterior;
    }

    public double getHiTemp() {
        return hiTemp;
    }

    public void setHiTemp(double hiTemp) {
        this.hiTemp = hiTemp;
    }

    public double getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(double lowTemp) {
        this.lowTemp = lowTemp;
    }

    public double getPuntoRocio() {
        return puntoRocio;
    }

    public void setPuntoRocio(double puntoRocio) {
        this.puntoRocio = puntoRocio;
    }

    public double getSensacionTermica() {
        return sensacionTermica;
    }

    public void setSensacionTermica(double sensacionTermica) {
        this.sensacionTermica = sensacionTermica;
    }

    public double getIndiceCalor() {
        return indiceCalor;
    }

    public void setIndiceCalor(double indiceCalor) {
        this.indiceCalor = indiceCalor;
    }

    public double getIndiceThw() {
        return indiceThw;
    }

    public void setIndiceThw(double indiceThw) {
        this.indiceThw = indiceThw;
    }

    public String getDireccionViento() {
        return direccionViento;
    }

    public void setDireccionViento(String direccionViento) {
        this.direccionViento = direccionViento == null ? "" : direccionViento;
    }

    public double getRecorridoViento() {
        return recorridoViento;
    }

    public void setRecorridoViento(double recorridoViento) {
        this.recorridoViento = recorridoViento;
    }

    public double getRafagaMaxima() {
        return rafagaMaxima;
    }

    public void setRafagaMaxima(double rafagaMaxima) {
        this.rafagaMaxima = rafagaMaxima;
    }

    public String getDireccionRafaga() {
        return direccionRafaga;
    }

    public void setDireccionRafaga(String direccionRafaga) {
        this.direccionRafaga = direccionRafaga == null ? "" : direccionRafaga;
    }

    public double getPresionBarometrica() {
        return presionBarometrica;
    }

    public void setPresionBarometrica(double presionBarometrica) {
        this.presionBarometrica = presionBarometrica;
    }

    public double getTasaLluvia() {
        return tasaLluvia;
    }

    public void setTasaLluvia(double tasaLluvia) {
        this.tasaLluvia = tasaLluvia;
    }

    public double getGradosDiaCalefaccion() {
        return gradosDiaCalefaccion;
    }

    public void setGradosDiaCalefaccion(double gradosDiaCalefaccion) {
        this.gradosDiaCalefaccion = gradosDiaCalefaccion;
    }

    public double getGradosDiaRefrigeracion() {
        return gradosDiaRefrigeracion;
    }

    public void setGradosDiaRefrigeracion(double gradosDiaRefrigeracion) {
        this.gradosDiaRefrigeracion = gradosDiaRefrigeracion;
    }

    public double getTempInterior() {
        return tempInterior;
    }

    public void setTempInterior(double tempInterior) {
        this.tempInterior = tempInterior;
    }

    public double getHumedadInterior() {
        return humedadInterior;
    }

    public void setHumedadInterior(double humedadInterior) {
        this.humedadInterior = humedadInterior;
    }

    public double getPuntoRocioInterior() {
        return puntoRocioInterior;
    }

    public void setPuntoRocioInterior(double puntoRocioInterior) {
        this.puntoRocioInterior = puntoRocioInterior;
    }

    public double getIndiceCalorInterior() {
        return indiceCalorInterior;
    }

    public void setIndiceCalorInterior(double indiceCalorInterior) {
        this.indiceCalorInterior = indiceCalorInterior;
    }

    public double getEmcInterior() {
        return emcInterior;
    }

    public void setEmcInterior(double emcInterior) {
        this.emcInterior = emcInterior;
    }

    public double getDensidadAireInterior() {
        return densidadAireInterior;
    }

    public void setDensidadAireInterior(double densidadAireInterior) {
        this.densidadAireInterior = densidadAireInterior;
    }

    public int getMuestrasViento() {
        return muestrasViento;
    }

    public void setMuestrasViento(int muestrasViento) {
        this.muestrasViento = muestrasViento;
    }

    public int getEstadoTransmision() {
        return estadoTransmision;
    }

    public void setEstadoTransmision(int estadoTransmision) {
        this.estadoTransmision = estadoTransmision;
    }

    public int getIntervaloArchivo() {
        return intervaloArchivo;
    }

    public void setIntervaloArchivo(int intervaloArchivo) {
        this.intervaloArchivo = intervaloArchivo;
    }

    public double getCalidadRecepcion() {
        return calidadRecepcion;
    }

    public void setCalidadRecepcion(double calidadRecepcion) {
        this.calidadRecepcion = calidadRecepcion;
    }

    public void setHumedad(double humedad) {
        this.humedad = humedad;
    }

    public double getVelocidadViento() {
        return velocidadViento;
    }

    public void setVelocidadViento(double velocidadViento) {
        this.velocidadViento = velocidadViento;
    }

    public double getLluvia() {
        return lluvia;
    }

    public void setLluvia(double lluvia) {
        this.lluvia = lluvia;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
