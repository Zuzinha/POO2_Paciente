import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class SistemaClinica extends Application {

    private PacienteDAO dao = new PacienteDAO();
    private TableView<Paciente> tabela = new TableView<>();
    private ObservableList<Paciente> dadosTabela;

    // Campos do formulário
    private TextField txtId = new TextField();
    private TextField txtNome = new TextField();
    private TextField txtCpf = new TextField();
    private DatePicker dpNascimento = new DatePicker();
    private TextField txtTelefone = new TextField();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sistema de Gestão - Clínica Médica (PostgreSQL)");

        // --- Colunas da Tabela ---
        TableColumn<Paciente, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Paciente, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setMinWidth(180);

        TableColumn<Paciente, String> colCpf = new TableColumn<>("CPF");
        colCpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));

        TableColumn<Paciente, LocalDate> colData = new TableColumn<>("Nascimento");
        colData.setCellValueFactory(new PropertyValueFactory<>("dataNascimento"));

        TableColumn<Paciente, String> colTel = new TableColumn<>("Telefone");
        colTel.setCellValueFactory(new PropertyValueFactory<>("telefone"));

        tabela.getColumns().addAll(colId, colNome, colCpf, colData, colTel);

        // Ao clicar na tabela, preenche os campos
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                preencherFormulario(newSelection);
            }
        });

        // --- Formulário ---
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));

        txtId.setEditable(false);
        txtId.setDisable(true); // O usuário não deve mexer no ID
        txtId.setPromptText("Gerado pelo Banco");

        formGrid.add(new Label("ID:"), 0, 0);
        formGrid.add(txtId, 1, 0);
        formGrid.add(new Label("Nome:"), 0, 1);
        formGrid.add(txtNome, 1, 1);
        formGrid.add(new Label("CPF:"), 0, 2);
        formGrid.add(txtCpf, 1, 2);
        formGrid.add(new Label("Nascimento:"), 2, 1);
        formGrid.add(dpNascimento, 3, 1);
        formGrid.add(new Label("Telefone:"), 2, 2);
        formGrid.add(txtTelefone, 3, 2);

        // --- Botões ---
        Button btnCadastrar = new Button("Cadastrar");
        Button btnAtualizar = new Button("Atualizar");
        Button btnExcluir = new Button("Excluir");
        Button btnLimpar = new Button("Limpar / Novo");

        HBox botoesBox = new HBox(10, btnCadastrar, btnAtualizar, btnExcluir, btnLimpar);
        botoesBox.setPadding(new Insets(10, 0, 10, 0));

        // --- Área de Busca ---
        TextField txtBusca = new TextField();
        txtBusca.setPromptText("Buscar nome...");
        Button btnBuscar = new Button("Pesquisar");

        HBox buscaBox = new HBox(10, new Label("Filtrar:"), txtBusca, btnBuscar);

        // --- Eventos ---

        btnCadastrar.setOnAction(e -> {
            if (validarCampos()) {
                // ID é 0 pois o banco vai gerar um novo
                Paciente p = new Paciente(0, txtNome.getText(), txtCpf.getText(), dpNascimento.getValue(), txtTelefone.getText());
                dao.salvarPaciente(p);
                carregarDados();
                limparCampos();
            }
        });

        btnAtualizar.setOnAction(e -> {
            if (txtId.getText().isEmpty()) {
                alerta("Selecione um paciente na tabela para editar.");
                return;
            }
            if (validarCampos()) {
                int idAtual = Integer.parseInt(txtId.getText());
                Paciente p = new Paciente(idAtual, txtNome.getText(), txtCpf.getText(), dpNascimento.getValue(), txtTelefone.getText());
                dao.atualizarPaciente(p);
                carregarDados();
                limparCampos();
            }
        });

        btnExcluir.setOnAction(e -> {
            if (txtId.getText().isEmpty()) {
                alerta("Selecione um paciente para excluir.");
                return;
            }
            dao.excluirPaciente(Integer.parseInt(txtId.getText()));
            carregarDados();
            limparCampos();
        });

        btnBuscar.setOnAction(e -> {
            dadosTabela = FXCollections.observableArrayList(dao.buscarPacientePorNome(txtBusca.getText()));
            tabela.setItems(dadosTabela);
        });

        btnLimpar.setOnAction(e -> {
            limparCampos();
            carregarDados(); // Reseta a busca
        });

        // Carrega dados iniciais
        carregarDados();

        // Layout final
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.getChildren().addAll(formGrid, botoesBox, new Separator(), buscaBox, tabela);

        Scene scene = new Scene(layout, 700, 550);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- Métodos Auxiliares ---

    private void carregarDados() {
        dadosTabela = FXCollections.observableArrayList(dao.listarPacientes());
        tabela.setItems(dadosTabela);
    }

    private void preencherFormulario(Paciente p) {
        txtId.setText(String.valueOf(p.getId()));
        txtNome.setText(p.getNome());
        txtCpf.setText(p.getCpf());
        dpNascimento.setValue(p.getDataNascimento());
        txtTelefone.setText(p.getTelefone());
    }

    private void limparCampos() {
        txtId.clear();
        txtNome.clear();
        txtCpf.clear();
        dpNascimento.setValue(null);
        txtTelefone.clear();
        tabela.getSelectionModel().clearSelection();
    }

    private boolean validarCampos() {
        if (txtNome.getText().isEmpty() || txtCpf.getText().isEmpty() || dpNascimento.getValue() == null) {
            alerta("Preencha Nome, CPF e Data de Nascimento!");
            return false;
        }
        return true;
    }

    private void alerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}