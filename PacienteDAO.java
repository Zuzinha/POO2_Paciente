import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

    // --- CONFIGURAÇÕES DO BANCO DE DADOS ---
    private final String URL = "jdbc:postgresql://localhost:5432/clinica";
    private final String USER = "postgres";
    private final String PASSWORD = "admin"; // <--- COLOQUE SUA SENHA DO POSTGRES AQUI

    // Metodo auxiliar para criar a conexão
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // --- SALVAR (INSERT) ---
    public void salvarPaciente(Paciente paciente) {

        String sql = "INSERT INTO pacientes (nome, cpf, data_nascimento, telefone) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, paciente.getNome());
            stmt.setString(2, paciente.getCpf());

            stmt.setDate(3, java.sql.Date.valueOf(paciente.getDataNascimento()));
            stmt.setString(4, paciente.getTelefone());

            stmt.executeUpdate();
            System.out.println("Sucesso: Paciente salvo no banco.");

        } catch (SQLException e) {
            System.err.println("Erro ao salvar paciente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- LISTAR TODOS (SELECT) ---
    public List<Paciente> listarPacientes() {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM pacientes ORDER BY id ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Paciente p = criarPacienteDoResultSet(rs);
                lista.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar pacientes: " + e.getMessage());
        }
        return lista;
    }

    // --- BUSCAR POR NOME (SELECT com Filtro) ---
    public List<Paciente> buscarPacientePorNome(String termo) {
        List<Paciente> lista = new ArrayList<>();

        String sql = "SELECT * FROM pacientes WHERE nome ILIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + termo + "%"); // O % permite buscar partes do nome
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(criarPacienteDoResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar paciente: " + e.getMessage());
        }
        return lista;
    }

    // --- ATUALIZAR (UPDATE) ---
    public void atualizarPaciente(Paciente paciente) {
        String sql = "UPDATE pacientes SET nome=?, cpf=?, data_nascimento=?, telefone=? WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, paciente.getNome());
            stmt.setString(2, paciente.getCpf());
            stmt.setDate(3, java.sql.Date.valueOf(paciente.getDataNascimento()));
            stmt.setString(4, paciente.getTelefone());
            stmt.setInt(5, paciente.getId()); // O ID é usado para saber QUEM atualizar

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("Sucesso: Paciente atualizado.");
            } else {
                System.out.println("Aviso: Nenhum paciente encontrado com esse ID.");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar paciente: " + e.getMessage());
        }
    }

    // --- EXCLUIR (DELETE) ---
    public void excluirPaciente(int id) {
        String sql = "DELETE FROM pacientes WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("Sucesso: Paciente excluído.");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao excluir paciente: " + e.getMessage());
        }
    }

    // Metodo auxiliar para evitar repetição de código ao ler os dados
    private Paciente criarPacienteDoResultSet(ResultSet rs) throws SQLException {
        return new Paciente(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("cpf"),
                rs.getDate("data_nascimento").toLocalDate(),
                rs.getString("telefone")
        );
    }
}