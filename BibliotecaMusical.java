import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CASO DE USO: "Prestar un instrumento musical raro"
 * Biblioteca de instrumentos antiguos y poco comunes (viola da gamba,
 * serpentón barroco, tabla india...).
 *
 * Actores:
 *   - Bibliotecario (principal): registra el préstamo en el sistema.
 *   - Socio: pide el instrumento y recibe el comprobante.
 *
 * Flujo principal:
 *   1. El bibliotecario indica el id del socio y el código del instrumento.
 *   2. El sistema busca al socio y al instrumento.
 *   3. Valida que el socio esté activo y tenga menos de 2 préstamos.
 *   4. Valida que el instrumento esté disponible.
 *   5. Calcula la fecha de devolución y la garantía según la familia
 *      (cuerda 14 días / 10 %, viento 7 días / 15 %, percusión 21 días / 5 %).
 *   6. Registra el préstamo y marca el instrumento como prestado.
 *   7. Envía el comprobante al socio.
 * Flujos alternos: socio o instrumento inexistente, socio inactivo o con
 * el máximo de préstamos, instrumento ya prestado -> se muestra un error.
 *
 * Este archivo tiene dos versiones del mismo caso de uso:
 *   - Antes:   todo en una "clase Dios" (SistemaPrestamos).
 *   - Despues: refactorizada con los 9 patrones GRASP.
 *
 * Compilar y ejecutar:
 *   javac -encoding UTF-8 BibliotecaMusical.java
 *   java BibliotecaMusical
 */
public class BibliotecaMusical {

    public static void main(String[] args) {
        System.out.println("===== ANTES DE GRASP =====");
        Antes.ejecutar();
        System.out.println();
        System.out.println("===== DESPUÉS DE GRASP =====");
        Despues.ejecutar();
    }

    // =====================================================================
    // VERSIÓN 1: ANTES DE APLICAR GRASP
    // Problemas: campos públicos, un switch por tipo de instrumento y una
    // sola clase que busca, valida, calcula, crea, guarda e imprime.
    // =====================================================================
    static class Antes {

        /** Datos sueltos: cualquiera puede modificarlos desde fuera. */
        static class Socio {
            public String id;
            public String nombre;
            public boolean activo;
            public int prestamosActivos;

            Socio(String id, String nombre, boolean activo) {
                this.id = id;
                this.nombre = nombre;
                this.activo = activo;
            }
        }

        /** El tipo es un String: cada regla nueva obliga a tocar el switch. */
        static class Instrumento {
            public String codigo;
            public String nombre;
            public String tipo; // "CUERDA", "VIENTO" o "PERCUSION"
            public double valorTasado;
            public boolean prestado;

            Instrumento(String codigo, String nombre, String tipo, double valorTasado) {
                this.codigo = codigo;
                this.nombre = nombre;
                this.tipo = tipo;
                this.valorTasado = valorTasado;
            }
        }

        /** Solo guarda datos; otro lo rellena desde fuera. */
        static class Prestamo {
            public Socio socio;
            public Instrumento instrumento;
            public LocalDate fechaInicio;
            public LocalDate fechaDevolucion;
            public double garantia;
        }

        /** "Clase Dios": hace todo el caso de uso ella sola. */
        static class SistemaPrestamos {
            public List<Socio> socios = new ArrayList<>();
            public List<Instrumento> instrumentos = new ArrayList<>();
            public List<Prestamo> prestamos = new ArrayList<>();

            public void prestarInstrumento(String idSocio, String codigoInstrumento) {
                // 1. Buscar (recorre listas directamente)
                Socio socio = null;
                for (Socio s : socios) {
                    if (s.id.equals(idSocio)) {
                        socio = s;
                    }
                }
                Instrumento instrumento = null;
                for (Instrumento i : instrumentos) {
                    if (i.codigo.equals(codigoInstrumento)) {
                        instrumento = i;
                    }
                }

                // 2. Validar leyendo campos ajenos
                if (socio == null || instrumento == null) {
                    System.out.println("Error: socio o instrumento inexistente");
                    return;
                }
                if (!socio.activo || socio.prestamosActivos >= 2) {
                    System.out.println("Error: " + socio.nombre + " no puede pedir más préstamos");
                    return;
                }
                if (instrumento.prestado) {
                    System.out.println("Error: " + instrumento.nombre + " ya está prestado");
                    return;
                }

                // 3. Calcular con un switch (cada tipo nuevo = editar aquí)
                int dias;
                double porcentaje;
                switch (instrumento.tipo) {
                    case "CUERDA":
                        dias = 14;
                        porcentaje = 0.10;
                        break;
                    case "VIENTO":
                        dias = 7;
                        porcentaje = 0.15;
                        break;
                    case "PERCUSION":
                        dias = 21;
                        porcentaje = 0.05;
                        break;
                    default:
                        dias = 7;
                        porcentaje = 0.20;
                }

                // 4. Crear el préstamo rellenando sus campos desde fuera
                Prestamo prestamo = new Prestamo();
                prestamo.socio = socio;
                prestamo.instrumento = instrumento;
                prestamo.fechaInicio = LocalDate.now();
                prestamo.fechaDevolucion = prestamo.fechaInicio.plusDays(dias);
                prestamo.garantia = instrumento.valorTasado * porcentaje;

                // 5. Guardar y cambiar el estado de otros objetos
                prestamos.add(prestamo);
                instrumento.prestado = true;
                socio.prestamosActivos++;

                // 6. Imprimir (canal fijo, no se puede cambiar)
                System.out.println("Comprobante: " + socio.nombre + " lleva " + instrumento.nombre
                        + " hasta " + prestamo.fechaDevolucion + ", garantía $" + prestamo.garantia);
            }
        }

        static void ejecutar() {
            SistemaPrestamos sistema = new SistemaPrestamos();
            sistema.socios.add(new Socio("S1", "Lucía Rojas", true));
            sistema.socios.add(new Socio("S2", "Tomás Vélez", false));
            sistema.instrumentos.add(new Instrumento("I1", "Viola da gamba 1720", "CUERDA", 9_000_000));
            sistema.instrumentos.add(new Instrumento("I2", "Serpentón barroco", "VIENTO", 6_000_000));
            sistema.instrumentos.add(new Instrumento("I3", "Tabla india antigua", "PERCUSION", 2_000_000));

            sistema.prestarInstrumento("S1", "I1");
            sistema.prestarInstrumento("S1", "I1"); // ya prestado
            sistema.prestarInstrumento("S2", "I2"); // socio inactivo
            sistema.prestarInstrumento("S1", "I3");
            sistema.prestarInstrumento("S1", "I2"); // supera el máximo
        }
    }

    // =====================================================================
    // VERSIÓN 2: DESPUÉS DE APLICAR LOS 9 PATRONES GRASP
    // Capas: dominio -> persistencia -> infraestructura -> aplicación -> presentación
    // =====================================================================
    static class Despues {

        // ---------------------------- DOMINIO ----------------------------

        /**
         * POLIMORFISMO: cada subclase da sus días y su porcentaje; no hay switch.
         * EXPERTO EN INFORMACIÓN: tiene el valor tasado y la disponibilidad,
         * así que calcula la garantía, la fecha de devolución y si está libre.
         */
        abstract static class Instrumento {
            private final String codigo;
            private final String nombre;
            private final double valorTasado;
            private boolean disponible = true;

            Instrumento(String codigo, String nombre, double valorTasado) {
                this.codigo = codigo;
                this.nombre = nombre;
                this.valorTasado = valorTasado;
            }

            abstract String tipo();

            abstract int diasMaximoPrestamo();

            abstract double porcentajeGarantia();

            double calcularGarantia() {
                return valorTasado * porcentajeGarantia();
            }

            LocalDate calcularFechaDevolucion(LocalDate inicio) {
                return inicio.plusDays(diasMaximoPrestamo());
            }

            boolean estaDisponible() {
                return disponible;
            }

            void marcarPrestado() {
                disponible = false;
            }

            String getCodigo() {
                return codigo;
            }

            String getNombre() {
                return nombre;
            }
        }

        /** POLIMORFISMO: cuerda, 14 días y 10 % de garantía. */
        static class InstrumentoCuerda extends Instrumento {
            InstrumentoCuerda(String codigo, String nombre, double valorTasado) {
                super(codigo, nombre, valorTasado);
            }

            @Override
            String tipo() {
                return "Cuerda";
            }

            @Override
            int diasMaximoPrestamo() {
                return 14;
            }

            @Override
            double porcentajeGarantia() {
                return 0.10;
            }
        }

        /** POLIMORFISMO: viento, más delicado: 7 días y 15 % de garantía. */
        static class InstrumentoViento extends Instrumento {
            InstrumentoViento(String codigo, String nombre, double valorTasado) {
                super(codigo, nombre, valorTasado);
            }

            @Override
            String tipo() {
                return "Viento";
            }

            @Override
            int diasMaximoPrestamo() {
                return 7;
            }

            @Override
            double porcentajeGarantia() {
                return 0.15;
            }
        }

        /** POLIMORFISMO: percusión, más resistente: 21 días y 5 % de garantía. */
        static class InstrumentoPercusion extends Instrumento {
            InstrumentoPercusion(String codigo, String nombre, double valorTasado) {
                super(codigo, nombre, valorTasado);
            }

            @Override
            String tipo() {
                return "Percusión";
            }

            @Override
            int diasMaximoPrestamo() {
                return 21;
            }

            @Override
            double porcentajeGarantia() {
                return 0.05;
            }
        }

        /**
         * EXPERTO EN INFORMACIÓN: se arma pidiéndole fecha y garantía a su
         * instrumento. Solo Socio lo crea (ver CREADOR).
         */
        static class Prestamo {
            private final Socio socio;
            private final Instrumento instrumento;
            private final LocalDate fechaInicio;
            private final LocalDate fechaDevolucion;
            private final double garantia;

            Prestamo(Socio socio, Instrumento instrumento, LocalDate fechaInicio) {
                this.socio = socio;
                this.instrumento = instrumento;
                this.fechaInicio = fechaInicio;
                this.fechaDevolucion = instrumento.calcularFechaDevolucion(fechaInicio);
                this.garantia = instrumento.calcularGarantia();
            }

            Socio getSocio() {
                return socio;
            }

            Instrumento getInstrumento() {
                return instrumento;
            }

            LocalDate getFechaInicio() {
                return fechaInicio;
            }

            LocalDate getFechaDevolucion() {
                return fechaDevolucion;
            }

            double getGarantia() {
                return garantia;
            }
        }

        /**
         * CREADOR: el socio contiene sus préstamos, por eso los crea.
         * EXPERTO EN INFORMACIÓN: conoce su estado y sus préstamos,
         * así que decide si puede pedir otro.
         */
        static class Socio {
            static final int MAXIMO_PRESTAMOS = 2;

            private final String id;
            private final String nombre;
            private final boolean activo;
            private final List<Prestamo> prestamos = new ArrayList<>();

            Socio(String id, String nombre, boolean activo) {
                this.id = id;
                this.nombre = nombre;
                this.activo = activo;
            }

            boolean puedePedirPrestado() {
                return activo && prestamos.size() < MAXIMO_PRESTAMOS;
            }

            /** CREADOR: único lugar donde se hace new Prestamo(...). */
            Prestamo registrarPrestamo(Instrumento instrumento, LocalDate fechaInicio) {
                Prestamo prestamo = new Prestamo(this, instrumento, fechaInicio);
                prestamos.add(prestamo);
                instrumento.marcarPrestado();
                return prestamo;
            }

            String getId() {
                return id;
            }

            String getNombre() {
                return nombre;
            }
        }

        // -------------------------- PERSISTENCIA --------------------------

        /** INDIRECCIÓN: el controlador busca socios sin saber dónde se guardan. */
        interface IRepositorioSocios {
            Optional<Socio> buscarPorId(String id);

            void guardar(Socio socio);
        }

        /** INDIRECCIÓN: el controlador busca instrumentos sin saber dónde se guardan. */
        interface IRepositorioInstrumentos {
            Optional<Instrumento> buscarPorCodigo(String codigo);

            void guardar(Instrumento instrumento);
        }

        /** FABRICACIÓN PURA: clase técnica que guarda socios en memoria. */
        static class RepositorioSociosMemoria implements IRepositorioSocios {
            private final List<Socio> socios = new ArrayList<>();

            @Override
            public Optional<Socio> buscarPorId(String id) {
                for (Socio socio : socios) {
                    if (socio.getId().equals(id)) {
                        return Optional.of(socio);
                    }
                }
                return Optional.empty();
            }

            @Override
            public void guardar(Socio socio) {
                socios.add(socio);
            }
        }

        /** FABRICACIÓN PURA: clase técnica que guarda instrumentos en memoria. */
        static class RepositorioInstrumentosMemoria implements IRepositorioInstrumentos {
            private final List<Instrumento> instrumentos = new ArrayList<>();

            @Override
            public Optional<Instrumento> buscarPorCodigo(String codigo) {
                for (Instrumento instrumento : instrumentos) {
                    if (instrumento.getCodigo().equals(codigo)) {
                        return Optional.of(instrumento);
                    }
                }
                return Optional.empty();
            }

            @Override
            public void guardar(Instrumento instrumento) {
                instrumentos.add(instrumento);
            }
        }

        // ------------------------- INFRAESTRUCTURA -------------------------

        /**
         * VARIACIONES PROTEGIDAS: interfaz estable para enviar el comprobante.
         * Hoy es consola o correo simulado; mañana podría ser SMS sin tocar
         * el controlador.
         */
        interface INotificador {
            void enviar(String mensaje);
        }

        /** VARIACIONES PROTEGIDAS: variante que muestra el comprobante en consola. */
        static class NotificadorConsola implements INotificador {
            @Override
            public void enviar(String mensaje) {
                System.out.println(mensaje);
            }
        }

        /** VARIACIONES PROTEGIDAS: variante que simula el envío por correo. */
        static class NotificadorCorreoSimulado implements INotificador {
            private final String destinatario;

            NotificadorCorreoSimulado(String destinatario) {
                this.destinatario = destinatario;
            }

            @Override
            public void enviar(String mensaje) {
                System.out.println("[correo a " + destinatario + "] " + mensaje);
            }
        }

        // --------------------------- APLICACIÓN ---------------------------

        /**
         * FABRICACIÓN PURA: dar formato al comprobante no es tarea natural de
         * Socio ni de Instrumento, así que se inventa esta clase.
         * ALTA COHESIÓN: solo arma el texto del comprobante.
         */
        static class GeneradorComprobante {
            String generar(Prestamo prestamo) {
                return "Comprobante: " + prestamo.getSocio().getNombre()
                        + " lleva " + prestamo.getInstrumento().getNombre()
                        + " (" + prestamo.getInstrumento().tipo() + ")"
                        + " desde " + prestamo.getFechaInicio()
                        + " hasta " + prestamo.getFechaDevolucion()
                        + ", garantía $" + String.format("%,.0f", prestamo.getGarantia());
            }
        }

        /**
         * CONTROLADOR: recibe la operación del sistema "prestar instrumento"
         * y coordina, sin calcular reglas del negocio.
         * BAJO ACOPLAMIENTO: solo depende de interfaces (repositorios y notificador).
         * ALTA COHESIÓN: solo coordina este caso de uso.
         */
        static class ControladorPrestamos {
            private final IRepositorioSocios repositorioSocios;
            private final IRepositorioInstrumentos repositorioInstrumentos;
            private final INotificador notificador;
            private final GeneradorComprobante generador = new GeneradorComprobante();

            ControladorPrestamos(IRepositorioSocios repositorioSocios,
                                 IRepositorioInstrumentos repositorioInstrumentos,
                                 INotificador notificador) {
                this.repositorioSocios = repositorioSocios;
                this.repositorioInstrumentos = repositorioInstrumentos;
                this.notificador = notificador;
            }

            Prestamo prestarInstrumento(String idSocio, String codigoInstrumento) {
                // 1. Buscar a través de las interfaces (INDIRECCIÓN)
                Socio socio = repositorioSocios.buscarPorId(idSocio)
                        .orElseThrow(() -> new IllegalArgumentException("No existe el socio " + idSocio));
                Instrumento instrumento = repositorioInstrumentos.buscarPorCodigo(codigoInstrumento)
                        .orElseThrow(() -> new IllegalArgumentException("No existe el instrumento " + codigoInstrumento));

                // 2. Preguntar a los expertos (EXPERTO EN INFORMACIÓN)
                if (!socio.puedePedirPrestado()) {
                    throw new IllegalStateException(socio.getNombre() + " no puede pedir más préstamos");
                }
                if (!instrumento.estaDisponible()) {
                    throw new IllegalStateException(instrumento.getNombre() + " ya está prestado");
                }

                // 3. El socio crea el préstamo (CREADOR)
                Prestamo prestamo = socio.registrarPrestamo(instrumento, LocalDate.now());

                // 4. Enviar el comprobante por el canal configurado (VARIACIONES PROTEGIDAS)
                notificador.enviar(generador.generar(prestamo));
                return prestamo;
            }
        }

        // -------------------------- PRESENTACIÓN --------------------------

        /** BAJO ACOPLAMIENTO: la pantalla solo conoce al controlador. */
        static class PantallaPrestamos {
            private final ControladorPrestamos controlador;

            PantallaPrestamos(ControladorPrestamos controlador) {
                this.controlador = controlador;
            }

            void solicitarPrestamo(String idSocio, String codigoInstrumento) {
                try {
                    controlador.prestarInstrumento(idSocio, codigoInstrumento);
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }

        /** Ensambla las piezas y ejecuta los mismos escenarios que la versión "antes". */
        static void ejecutar() {
            RepositorioSociosMemoria socios = new RepositorioSociosMemoria();
            socios.guardar(new Socio("S1", "Lucía Rojas", true));
            socios.guardar(new Socio("S2", "Tomás Vélez", false));

            RepositorioInstrumentosMemoria instrumentos = new RepositorioInstrumentosMemoria();
            instrumentos.guardar(new InstrumentoCuerda("I1", "Viola da gamba 1720", 9_000_000));
            instrumentos.guardar(new InstrumentoViento("I2", "Serpentón barroco", 6_000_000));
            instrumentos.guardar(new InstrumentoPercusion("I3", "Tabla india antigua", 2_000_000));

            // VARIACIONES PROTEGIDAS: cambiar esta línea por
            // new NotificadorCorreoSimulado("socios@biblioteca.org") no toca el controlador.
            INotificador notificador = new NotificadorConsola();

            ControladorPrestamos controlador = new ControladorPrestamos(socios, instrumentos, notificador);
            PantallaPrestamos pantalla = new PantallaPrestamos(controlador);

            pantalla.solicitarPrestamo("S1", "I1");
            pantalla.solicitarPrestamo("S1", "I1"); // ya prestado
            pantalla.solicitarPrestamo("S2", "I2"); // socio inactivo
            pantalla.solicitarPrestamo("S1", "I3");
            pantalla.solicitarPrestamo("S1", "I2"); // supera el máximo
        }
    }
}
