package weatherreport_mvc.models;

import java.time.LocalDateTime;

/**
 * Entidad que representa a un usuario del sistema.
 */
public class Usuario {

    public static final String ROL_INGENIERO = "Ingeniero";
    public static final String ROL_CONSULTA = "Consulta";
    /** Solo Panel Principal; es el único rol que se crea desde el registro del login. */
    public static final String ROL_BASICO = "Básico";

    private int id;
    private String nombreCompleto;
    private String usuario;
    private String contrasenaHash;
    private String rol;
    private LocalDateTime fechaRegistro;
    private boolean activo;

    public Usuario() {
        this.id = 0;
        this.activo = true;
    }

    public Usuario(int id, String nombreCompleto, String usuario,
                   String contrasenaHash, String rol,
                   LocalDateTime fechaRegistro, boolean activo) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.usuario = usuario;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getEstadoTexto() {
        return activo ? "Activo" : "Inactivo";
    }

    public boolean esIngeniero() {
        return ROL_INGENIERO.equals(rol);
    }

    public boolean esConsulta() {
        return ROL_CONSULTA.equals(rol);
    }

    public boolean esBasico() {
        return ROL_BASICO.equals(rol);
    }

    @Override
    public String toString() {
        return usuario;
    }
}
