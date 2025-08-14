import java.sql.*;
import java.util.*;

public class ClaseReceta {
    private Connection connection;

    public ClaseReceta(Connection connection) {
        this.connection = connection; //conexión a la base de datos proporcionada desde fuera
    }

    public List<Receta> obtenerRecetasGuardadas(int idUsuario) throws SQLException {
        List<Receta> recetas = new ArrayList<>();
        String sql = "SELECT r.* FROM RECETA r JOIN CONTROLAR c ON r.id_receta = c.id_receta " +
                     "WHERE c.id_usuario = ? AND c.guardado = TRUE";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario); //se enlaza el id del usuario para la consulta preparada
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                recetas.add(mapearRecetaDesdeResultSet(rs)); //mapea cada resultado a un objeto Receta
            }
        }
        return recetas; //devuelve la lista de recetas guardadas por el usuario
    }

    public Receta obtenerRecetaPorId(int idReceta) throws SQLException {
        String sql = "SELECT * FROM RECETA WHERE id_receta = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReceta); //consulta por id de receta
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapearRecetaDesdeResultSet(rs); //devuelve la receta encontrada
            }
        }
        return null; //retorna null si no se encuentra ninguna receta con ese id
    }

    public List<String> obtenerIngredientesConCantidad(int idReceta) throws SQLException {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT A.nombre, RI.cantidad FROM RECETA_INGREDIENTE RI " +
                     "JOIN ALIMENTO A ON RI.id_alimento = A.id_alimento WHERE RI.id_receta = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReceta); //consulta ingredientes de la receta
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(rs.getString("nombre") + " - " + rs.getString("cantidad")); //formato de salida como string simple
            }
        }
        return lista; //devuelve lista de strings con nombre y cantidad
    }

    public List<String[]> obtenerIngredientesYUnidades(int idReceta) throws SQLException {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT A.nombre, RI.cantidad FROM RECETA_INGREDIENTE RI " +
                     "JOIN ALIMENTO A ON RI.id_alimento = A.id_alimento WHERE RI.id_receta = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReceta); //consulta ingredientes con cantidades
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new String[] { rs.getString("nombre"), rs.getString("cantidad") }); //se añade como array para posible tratamiento separado de nombre y cantidad
            }
        }
        return lista; //devuelve lista de arrays con nombre y cantidad
    }

    public boolean estaGuardada(int idUsuario, int idReceta) throws SQLException {
        String sql = "SELECT guardado FROM CONTROLAR WHERE id_usuario = ? AND id_receta = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario); //se enlaza el id del usuario
            stmt.setInt(2, idReceta); //se enlaza el id de la receta
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getBoolean("guardado"); //devuelve true si existe y está guardada
        }
    }


    public void guardarReceta(int idUsuario, int idReceta) throws SQLException {
        String sql = "INSERT INTO CONTROLAR (id_usuario, id_receta, guardado, visto) VALUES (?, ?, TRUE, TRUE) " +
                    "ON DUPLICATE KEY UPDATE guardado = TRUE, visto = TRUE"; //inserta o actualiza la receta como guardada y vista
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario); //asocia el usuario
            stmt.setInt(2, idReceta); //asocia la receta
            stmt.executeUpdate(); //ejecuta la inserción o actualización
        }
    }

    public void desguardarReceta(int idUsuario, int idReceta) throws SQLException {
        String sql = "UPDATE CONTROLAR SET guardado = FALSE WHERE id_usuario = ? AND id_receta = ?"; //marca la receta como no guardada
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idReceta);
            stmt.executeUpdate(); //actualiza el valor del campo guardado
        }
    }

    public void añadirRecetaAlHistorial(int idUsuario, int idReceta) throws SQLException {
        String sqlVerificar = "SELECT 1 FROM CONTROLAR WHERE id_usuario = ? AND id_receta = ?"; //verifica si ya existe la relación
        try (PreparedStatement stmt = connection.prepareStatement(sqlVerificar)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idReceta);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                //no existía: insertar nueva entrada con visto=true y guardado=false
                String sqlInsertar = "INSERT INTO CONTROLAR (id_usuario, id_receta, visto, guardado) VALUES (?, ?, TRUE, FALSE)";
                try (PreparedStatement insertStmt = connection.prepareStatement(sqlInsertar)) {
                    insertStmt.setInt(1, idUsuario);
                    insertStmt.setInt(2, idReceta);
                    insertStmt.executeUpdate();
                }
            } else {
                //ya existe: actualizar campo visto a true si no lo estaba
                String sqlActualizar = "UPDATE CONTROLAR SET visto = TRUE WHERE id_usuario = ? AND id_receta = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(sqlActualizar)) {
                    updateStmt.setInt(1, idUsuario);
                    updateStmt.setInt(2, idReceta);
                    updateStmt.executeUpdate();
                }
            }
        }
    }

    public String obtenerPasosProcedimiento(int idReceta) throws SQLException {
        String sql = "SELECT procedimiento FROM RECETA WHERE id_receta = ?"; //consulta los pasos de la receta
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReceta);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("procedimiento") : ""; //devuelve los pasos o cadena vacía si no hay resultados
        }
    }

    public boolean yaHaValorado(int idUsuario, int idReceta) throws SQLException {
        String sql = "SELECT 1 FROM VALORACION WHERE id_usuario = ? AND id_receta = ?"; //verifica si el usuario ya valoró la receta
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idReceta);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); //retorna true si ya hay una valoración
        }
    }

    public void registrarValoracion(int idUsuario, int idReceta, double valoracion) throws SQLException {
        String insertSql = "INSERT INTO VALORACION (id_usuario, id_receta, valoracion) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE valoracion = ?"; //inserta o actualiza la valoración
        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idReceta);
            stmt.setDouble(3, valoracion);
            stmt.setDouble(4, valoracion);
            stmt.executeUpdate(); //guarda o actualiza la valoración
        }
        actualizarPromedioYVotos(idReceta); //recalcula media y número de votos
    }

    private void actualizarPromedioYVotos(int idReceta) throws SQLException {
        String updateSql = "UPDATE RECETA SET " +
                        "valoracion = (SELECT AVG(valoracion) FROM VALORACION WHERE id_receta = ?), " +
                        "votos = (SELECT COUNT(*) FROM VALORACION WHERE id_receta = ?) " +
                        "WHERE id_receta = ?"; //actualiza el promedio de valoración y el número de votos
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setInt(1, idReceta);
            stmt.setInt(2, idReceta);
            stmt.setInt(3, idReceta);
            stmt.executeUpdate(); //ejecuta la actualización de la tabla RECETA
        }
    }


    public double obtenerMediaValoracion(int idReceta) throws SQLException {
        String sql = "SELECT AVG(valoracion) AS media FROM VALORACION WHERE id_receta = ?"; //calcula la media de valoraciones de una receta
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReceta);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble("media") : 0.0; //si hay resultado, devuelve la media; si no, devuelve 0.0
        }
    }

    public int obtenerVotos(int idReceta) throws SQLException {
        String sql = "SELECT votos FROM RECETA WHERE id_receta = ?"; //obtiene el número de votos desde la tabla receta
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReceta);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("votos") : 0; //retorna el número de votos o 0 si no hay resultados
        }
    }

    public byte[] obtenerImagenReceta(int idReceta) throws SQLException {
        String query = "SELECT imagen FROM RECETA WHERE id_receta = ?"; //obtiene los bytes de imagen desde la base de datos
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idReceta);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBytes("imagen"); //devuelve el arreglo de bytes de la imagen
            }
        }
        return null; //si no se encuentra, retorna null
    }

    public List<Receta> buscarRecetasPorNombreOCreador(String texto) throws SQLException {
        List<Receta> recetas = new ArrayList<>();
        String sql = "SELECT r.* FROM RECETA r JOIN USUARIO u ON r.creador = u.id_usuario " +
                    "WHERE r.nombre LIKE ? OR u.nombre LIKE ?"; //busca recetas cuyo nombre o creador empiece con el texto
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, texto + "%"); //búsqueda por nombre de receta
            stmt.setString(2, texto + "%"); //búsqueda por nombre del creador
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                recetas.add(mapearRecetaDesdeResultSet(rs)); //convierte los resultados a objetos Receta
            }
        }
        return recetas; //retorna la lista de recetas encontradas
    }

    public List<Receta> obtenerRecetasVistas(int idUsuario) throws SQLException {
        List<Receta> recetas = new ArrayList<>();
        String sql = "SELECT r.* FROM CONTROLAR c " +
                    "JOIN RECETA r ON c.id_receta = r.id_receta " +
                    "WHERE c.id_usuario = ? AND c.visto = TRUE"; //busca todas las recetas vistas por el usuario
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                recetas.add(mapearRecetaDesdeResultSet(rs)); //mapea cada fila a un objeto Receta
            }
        }
        return recetas;
    }

    public void borrarHistorial(int idUsuario) throws SQLException {
        String sql = "UPDATE CONTROLAR SET visto = FALSE WHERE id_usuario = ?"; //marca todas las recetas como no vistas para el usuario
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.executeUpdate(); //ejecuta la actualización del historial
        }
    }

    private Receta mapearRecetaDesdeResultSet(ResultSet rs) throws SQLException {
        return new Receta(
            rs.getInt("id_receta"), //id de la receta
            rs.getString("nombre"), //nombre de la receta
            rs.getInt("creador"), //id del usuario creador
            rs.getString("dificultad"), //nivel de dificultad
            rs.getInt("tiempo_preparacion"), //tiempo estimado en minutos
            rs.getBoolean("vegano"), //si es vegana
            rs.getBoolean("vegetariano"), //si es vegetariana
            rs.getBoolean("celiaco"), //si es apta para celíacos
            rs.getString("procedimiento"), //pasos de preparación
            rs.getBytes("imagen"), //imagen en formato byte[]
            rs.getInt("votos"), //número total de votos
            rs.getFloat("valoracion"), //valoración promedio
            rs.getInt("valor_energetico"), //calorías o valor energético
            rs.getDouble("proteinas"), //cantidad de proteínas
            rs.getDouble("glucidos"), //cantidad de glúcidos
            rs.getDouble("lipidos") //cantidad de lípidos
        );
    }

    public int guardarReceta(Receta receta) throws SQLException {
        String sql = "INSERT INTO RECETA (nombre, creador, dificultad, tiempo_preparacion, vegano, vegetariano, celiaco, procedimiento, imagen, valor_energetico, proteinas, glucidos, lipidos) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, receta.getNombre()); //establece el nombre de la receta
            stmt.setInt(2, receta.getCreadorId()); //id del usuario creador
            stmt.setString(3, receta.getDificultad()); //nivel de dificultad
            stmt.setInt(4, receta.getTiempoPreparacion()); //tiempo estimado de preparación
            stmt.setBoolean(5, receta.isVegano()); //si es vegana
            stmt.setBoolean(6, receta.isVegetariano()); //si es vegetariana
            stmt.setBoolean(7, receta.isCeliaco()); //si es apta para celíacos
            stmt.setString(8, receta.getProcedimiento()); //pasos del procedimiento
            stmt.setBytes(9, receta.getImagen()); //imagen de la receta en bytes (BLOB)
            stmt.setInt(10, receta.getValorEnergetico()); //calorías
            stmt.setDouble(11, receta.getProteinas()); //proteínas
            stmt.setDouble(12, receta.getGlucidos()); //glúcidos
            stmt.setDouble(13, receta.getLipidos()); //lípidos

            stmt.executeUpdate(); //ejecuta la inserción en la base de datos
            ResultSet rs = stmt.getGeneratedKeys(); //obtiene la clave primaria generada automáticamente
            if (rs.next()) {
                return rs.getInt(1); //retorna el id de la receta recién creada
            }
        }
        throw new SQLException("No se pudo guardar la receta."); //error si no se obtiene id generado
    }

    public void guardarIngrediente(int idReceta, int idAlimento, String cantidad) throws SQLException {
        String sql = "INSERT INTO RECETA_INGREDIENTE (id_receta, id_alimento, cantidad) VALUES (?, ?, ?)"; //relación receta-alimento con cantidad
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReceta); //id de la receta
            stmt.setInt(2, idAlimento); //id del alimento
            stmt.setString(3, cantidad); //cantidad especificada (con unidad)
            stmt.executeUpdate(); //guarda la relación en la tabla RECETA_INGREDIENTE
        }
    }

    public void actualizarPropiedadesNutricionales(int idReceta, double valorEnergetico,
                                                boolean vegano, boolean vegetariano, boolean celiaco,
                                                double proteinas, double glucidos, double lipidos) throws SQLException {
        String sql = "UPDATE RECETA SET valor_energetico = ?, vegano = ?, vegetariano = ?, celiaco = ?, proteinas = ?, glucidos = ?, lipidos = ? WHERE id_receta = ?"; //actualiza valores nutricionales
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, valorEnergetico); //nueva cantidad de calorías
            stmt.setBoolean(2, vegano); //actualización de etiqueta vegano
            stmt.setBoolean(3, vegetariano); //actualización de etiqueta vegetariano
            stmt.setBoolean(4, celiaco); //actualización de etiqueta celiaco
            stmt.setDouble(5, proteinas); //nueva cantidad de proteínas
            stmt.setDouble(6, glucidos); //nueva cantidad de glúcidos
            stmt.setDouble(7, lipidos); //nueva cantidad de lípidos
            stmt.setInt(8, idReceta); //id de la receta a modificar

            stmt.executeUpdate(); //ejecuta la actualización
        }
    }

    public boolean existeAlimento(String nombreAlimento) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ALIMENTO WHERE LOWER(nombre) = LOWER(?)"; //verifica existencia ignorando mayúsculas
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nombreAlimento.trim()); //elimina espacios y normaliza el nombre
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0; //retorna true si hay al menos un alimento con ese nombre
        }
    }


    //método que devuelve recetas que pueden prepararse con los ingredientes disponibles del usuario
    public List<ResultadoBusqueda> buscarRecetasConResultado(List<AlimentoDisponible> disponibles) throws SQLException {
        List<ResultadoBusqueda> resultados = new ArrayList<>(); //lista que almacenará las recetas válidas
        List<Receta> todas = buscarRecetasPorNombreOCreador(""); //obtiene todas las recetas desde la base de datos

        for (Receta r : todas) {
            List<AlimentoDisponible> ingredientesNecesarios = obtenerIngredientesDeReceta(r.getId()); //ingredientes requeridos para esta receta
            boolean cantidadesSuficientes = true; //bandera para saber si se cumplen todos los requisitos

            for (AlimentoDisponible ingNecesario : ingredientesNecesarios) {
                String nombreReq = ingNecesario.getAlimento().getNombre().toLowerCase().trim(); //nombre del ingrediente requerido
                double cantidadReq = ingNecesario.getCantidad(); //cantidad requerida
                String unidadReq = ingNecesario.getUnidad().toLowerCase().trim(); //unidad requerida
                boolean encontrado = false; //bandera para saber si el usuario tiene este ingrediente

                for (AlimentoDisponible ad : disponibles) {
                    String nombreUsuario = ad.getAlimento().getNombre().toLowerCase().trim(); //nombre del ingrediente disponible

                    if (nombreReq.equals(nombreUsuario)) { //coincidencia por nombre
                        double cantidadUsuario = ad.getCantidad(); //cantidad disponible
                        String unidadUsuario = ad.getUnidad().toLowerCase().trim(); //unidad disponible

                        boolean unidadRecetaEsComodin = unidadReq.contains("ojo"); //la receta acepta unidad "a ojo"
                        boolean unidadUsuarioEsComodin = unidadUsuario.contains("ojo"); //el usuario ha indicado unidad "a ojo"

                        //si la unidad de la receta es "a ojo", automáticamente se acepta
                        if (unidadRecetaEsComodin) {
                            encontrado = true;
                            break;
                        }

                        //verifica compatibilidad entre unidades
                        boolean unidadCompatible =
                            unidadUsuario.equals(unidadReq) //mismas unidades
                            || (unidadUsuario.equals("unidad") && (unidadReq.equals("g") || unidadReq.equals("ml"))) //usuario tiene "unidad" y receta pide gramos/ml
                            || ((unidadUsuario.equals("g") || unidadUsuario.equals("ml")) && unidadReq.equals("unidad")); //usuario tiene gramos/ml y receta pide "unidad"

                        //si no son compatibles y no es "a ojo", se ignora este ingrediente disponible
                        if (!unidadCompatible && !unidadUsuarioEsComodin) {
                            continue;
                        }

                        //ajusta las cantidades si las unidades no coinciden
                        double cantidadUsuarioConvertida = cantidadUsuario;
                        double cantidadReqConvertida = cantidadReq;

                        //conversión de "unidad" a gramos/ml (estimado como 150)
                        if (unidadUsuario.equals("unidad") && (unidadReq.equals("g") || unidadReq.equals("ml"))) {
                            cantidadUsuarioConvertida = cantidadUsuario * 150;
                        } else if ((unidadUsuario.equals("g") || unidadUsuario.equals("ml")) && unidadReq.equals("unidad")) {
                            cantidadReqConvertida = cantidadReq * 150;
                        }

                        //si el usuario no indicó "a ojo", comparamos cantidades
                        if (!unidadUsuarioEsComodin) {
                            if (cantidadUsuarioConvertida < cantidadReqConvertida) {
                                cantidadesSuficientes = false; //no hay suficiente cantidad
                            }
                        }

                        encontrado = true; //se encontró un ingrediente compatible
                        break;
                    }
                }

                //si el ingrediente no fue encontrado, no se puede preparar la receta
                if (!encontrado) {
                    cantidadesSuficientes = false;
                    break;
                }
            }

            //si todos los ingredientes están presentes y en cantidad suficiente, se añade la receta al resultado
            if (cantidadesSuficientes) {
                resultados.add(new ResultadoBusqueda(r, true)); //la receta es válida para preparar
            }
        }

        return resultados; //devuelve la lista de resultados de búsqueda
    }


    //método que elimina una receta de la base de datos por su id
    public void eliminarReceta(int idReceta) throws SQLException {
        String sql = "DELETE FROM RECETA WHERE id_receta = ?"; //consulta para eliminar la receta
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, idReceta); //se enlaza el id de la receta a eliminar
        stmt.executeUpdate(); //se ejecuta la eliminación
    }

    //método que obtiene los ingredientes asociados a una receta como objetos AlimentoDisponible
    public List<AlimentoDisponible> obtenerIngredientesDeReceta(int idReceta) throws SQLException {
        List<AlimentoDisponible> lista = new ArrayList<>(); //lista que almacenará los ingredientes de la receta
        String sql = "SELECT A.id_alimento, A.nombre, A.valor_energetico, A.celiaco, A.vegano, A.vegetariano, " +
                    "A.proteinas, A.glucidos, A.lipidos, RI.cantidad " +
                    "FROM RECETA_INGREDIENTE RI " +
                    "JOIN ALIMENTO A ON RI.id_alimento = A.id_alimento " +
                    "WHERE RI.id_receta = ?"; //consulta para unir alimentos e ingredientes de la receta

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReceta); //se enlaza el id de la receta
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Alimento alimento = new Alimento( //se crea el objeto alimento
                    rs.getInt("id_alimento"),
                    rs.getString("nombre"),
                    rs.getInt("valor_energetico"),
                    rs.getBoolean("celiaco"),
                    rs.getBoolean("vegano"),
                    rs.getBoolean("vegetariano"),
                    rs.getDouble("proteinas"),
                    rs.getDouble("glucidos"),
                    rs.getDouble("lipidos")
                );

                String cantidadTexto = rs.getString("cantidad").trim(); //cadena de cantidad (e.g. "200 g")
                String[] partes = cantidadTexto.split("\\s+", 2); //separar número y unidad
                double cantidadNum = 0;
                String unidad = "";

                try {
                    cantidadNum = Double.parseDouble(partes[0]); //intenta convertir la primera parte a número
                    unidad = (partes.length > 1) ? partes[1].toLowerCase().trim() : "unidad"; //unidad por defecto si no se especifica
                } catch (NumberFormatException e) {
                    cantidadNum = 0; //si no se puede interpretar como número, se asigna 0
                    unidad = cantidadTexto.toLowerCase(); //usa toda la cadena como unidad
                }

                AlimentoDisponible disponible = new AlimentoDisponible(alimento, cantidadNum, unidad); //se construye el ingrediente con cantidad
                lista.add(disponible); //se añade a la lista final
            }
        }

        return lista; //devuelve la lista de ingredientes con cantidad y unidad
    }

    
    //método que verifica si una receta es apta para un usuario según su dieta y alimentos prohibidos
    public boolean esAptaPara(Receta receta, Usuario usuario, ClaseAlimento claseAlimento) throws SQLException {
        //verifica restricciones dietéticas del usuario
        if (usuario.isVegano() && !receta.isVegano()) return false;
        if (usuario.isVegetariano() && !receta.isVegetariano()) return false;
        if (usuario.isCeliaco() && !receta.isCeliaco()) return false;

        //obtiene los ingredientes de la receta
        List<AlimentoDisponible> ingredientes = obtenerIngredientesDeReceta(receta.getId());
        //obtiene la lista de alimentos prohibidos para el usuario
        List<Alimento> prohibidos = claseAlimento.obtenerAlimentosProhibidos(usuario.getId());

        Set<String> nombresProhibidos = new HashSet<>();
        for (Alimento a : prohibidos) {
            nombresProhibidos.add(a.getNombre().toLowerCase().trim()); //normaliza nombres para comparación
        }

        for (AlimentoDisponible ing : ingredientes) {
            String nombre = ing.getAlimento().getNombre().toLowerCase().trim();
            if (nombresProhibidos.contains(nombre)) return false; //si hay un ingrediente prohibido, no es apta
        }

        return true; //si pasó todas las comprobaciones, es apta
    }

    //método que devuelve todas las recetas creadas por un usuario específico
    public List<Receta> obtenerRecetasDelUsuario(int idUsuario) throws SQLException {
        List<Receta> recetas = new ArrayList<>();
        String sql = "SELECT * FROM RECETA WHERE creador = ?"; //consulta por recetas creadas por el usuario

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario); //id del usuario creador
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                recetas.add(mapearRecetaDesdeResultSet(rs)); //mapea cada receta desde el ResultSet
            }
        }

        return recetas; //devuelve la lista de recetas del usuario
    }

}
