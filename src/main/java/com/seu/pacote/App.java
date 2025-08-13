package com.seu.pacote;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class App extends Application {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/cadastro";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    private TableView<Obito> tableView;
    private ObservableList<Obito> obitosList;
    private Connection connection;

    public static class Obito {
        private int id;
        private int BE;
        private String nome_paciente;
        private LocalDate data_nascimento;
        private LocalDate dataObito;
        private String causaMorte;
        private String local_Obito;

        public Obito() {}

        public Obito(int BE, String nome_paciente, LocalDate data_nascimento,
                    LocalDate dataObito, String causaMorte, String local_Obito) {
            this.BE = BE;
            this.nome_paciente = nome_paciente;
            this.data_nascimento = data_nascimento;
            this.dataObito = dataObito;
            this.causaMorte = causaMorte;
            this.local_Obito = local_Obito;
        }

        // Getters e Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public int getBE() { return BE; }
        public void setBE(int BE) { this.BE = BE; }
        
        public String getNome_paciente() { return nome_paciente; }
        public void setNome_paciente(String nome_paciente) { this.nome_paciente = nome_paciente; }
        
        public LocalDate getData_nascimento() { return data_nascimento; }
        public void setData_nascimento(LocalDate data_nascimento) { this.data_nascimento = data_nascimento; }
        
        public LocalDate getDataObito() { return dataObito; }
        public void setDataObito(LocalDate dataObito) { this.dataObito = dataObito; }
        
        public String getCausaMorte() { return causaMorte; }
        public void setCausaMorte(String causaMorte) { this.causaMorte = causaMorte; }
        
        public String getLocal_Obito() { return local_Obito; }
        public void setLocal_Obito(String local_Obito) { this.local_Obito = local_Obito; }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Conexão com o banco de dados estabelecida com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erro de Conexão",
                    "Não foi possível conectar ao banco de dados: " + e.getMessage());
            return;
        }

        tableView = new TableView<>();
        obitosList = FXCollections.observableArrayList();

        // Configuração das colunas
        TableColumn<Obito, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Obito, Integer> beCol = new TableColumn<>("BE");
        beCol.setCellValueFactory(new PropertyValueFactory<>("BE"));

        TableColumn<Obito, String> nomeCol = new TableColumn<>("Nome do Paciente");
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome_paciente"));
        
        TableColumn<Obito, String> nascimentoCol = new TableColumn<>("Data de Nascimento");
        nascimentoCol.setCellValueFactory(new PropertyValueFactory<>("data_nascimento"));
        nascimentoCol.setCellFactory(column -> new TableCell<Obito, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        TableColumn<Obito, String> obitoCol = new TableColumn<>("Data Óbito");
        obitoCol.setCellValueFactory(new PropertyValueFactory<>("dataObito"));
        obitoCol.setCellFactory(column -> new TableCell<Obito, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        TableColumn<Obito, String> localCol = new TableColumn<>("Local do Óbito");
        localCol.setCellValueFactory(new PropertyValueFactory<>("local_Obito"));

        TableColumn<Obito, String> causaCol = new TableColumn<>("Causa da Morte");
        causaCol.setCellValueFactory(new PropertyValueFactory<>("causaMorte"));

        tableView.getColumns().addAll(idCol, beCol, nomeCol, nascimentoCol, obitoCol, localCol, causaCol);

        // Configuração dos campos de formulário
        TextField beField = new TextField();
        TextField nomeField = new TextField();
        DatePicker nascimentoPicker = new DatePicker();
        DatePicker obitoPicker = new DatePicker();
        TextField causaField = new TextField();
        TextField localField = new TextField();
        Button addButton = new Button("Adicionar");
        Button refreshButton = new Button("Atualizar Lista");
        Button deleteButton = new Button("Excluir");

        // Configuração do conversor de data mais robusto
        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, dateFormatter);
                    } catch (DateTimeParseException e) {
                        System.err.println("Data inválida: " + string);
                        return null;
                    }
                } else {
                    return null;
                }
            }
        };

        nascimentoPicker.setConverter(converter);
        obitoPicker.setConverter(converter);

        // Configuração do GridPane para o formulário
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(30);
        grid.setPadding(new Insets(20));

        grid.addRow(0, new Label("BE:"), beField);
        grid.addRow(1, new Label("Nome:"), nomeField);
        grid.addRow(2, new Label("Data Nascimento:"), nascimentoPicker);
        grid.addRow(3, new Label("Data Óbito:"), obitoPicker);
        grid.addRow(4, new Label("Causa Morte:"), causaField);
        grid.addRow(5, new Label("Local Óbito:"), localField);
        
        // Adiciona os botões na última linha
        GridPane buttonsPane = new GridPane();
        buttonsPane.setHgap(10);
        buttonsPane.addRow(0, addButton, refreshButton, deleteButton);
        grid.add(buttonsPane, 1, 6);

        // Configuração do layout principal
        VBox root = new VBox(10);
        root.getChildren().addAll(grid, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        
        Scene scene = new Scene(root, 900, 700);

        // Configuração dos eventos dos botões
        addButton.setOnAction(e -> {
            try {
                adicionarObito(
                    Integer.parseInt(beField.getText()),
                    nomeField.getText(),
                    nascimentoPicker.getValue(),
                    obitoPicker.getValue(),
                    causaField.getText(),
                    localField.getText()
                );
                // Limpa os campos após adicionar
                beField.clear();
                nomeField.clear();
                nascimentoPicker.setValue(null);
                obitoPicker.setValue(null);
                causaField.clear();
                localField.clear();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Erro de Formato", "BE deve ser um número inteiro");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro ao adicionar: " + ex.getMessage());
            }
        });

        refreshButton.setOnAction(e -> carregarObitos());
        deleteButton.setOnAction(e -> excluirObito());

        primaryStage.setTitle("Sistema de Óbitos");
        primaryStage.setScene(scene);
        primaryStage.show();

        carregarObitos();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void carregarObitos() {
        try {
            String sql = "SELECT * FROM obitos";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            List<Obito> obitos = new ArrayList<>();
            
            while (rs.next()) {
                Obito obito = new Obito();
                obito.setId(rs.getInt("id"));
                obito.setBE(rs.getInt("BE"));
                obito.setNome_paciente(rs.getString("nome_paciente"));
                
                // Tratamento mais seguro para datas
                Date dataNasc = rs.getDate("data_nascimento");
                obito.setData_nascimento(dataNasc != null ? dataNasc.toLocalDate() : null);
                
                Date dataObito = rs.getDate("dataObito");
                obito.setDataObito(dataObito != null ? dataObito.toLocalDate() : null);
                
                obito.setCausaMorte(rs.getString("causaMorte"));
                obito.setLocal_Obito(rs.getString("local_Obito"));
                obitos.add(obito);
            }
            
            obitosList.setAll(obitos);
            tableView.setItems(obitosList);
            System.out.println("Óbitos carregados com sucesso: " + obitos.size() + " registros");
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Erro ao carregar óbitos: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erro no Banco de Dados", 
                    "Não foi possível carregar os óbitos: " + e.getMessage());
        }
    }

    private void adicionarObito(int BE, String nome, LocalDate nascimento, LocalDate obito, String causa, String local) {
        if (nome == null || nome.isEmpty() || nascimento == null || obito == null ||
            causa == null || causa.isEmpty() || local == null || local.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", "Todos os campos são obrigatórios");
            return;
        }
        
        try {
            String sql = "INSERT INTO obitos (BE, nome_paciente, data_nascimento, dataObito, causaMorte, local_Obito) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, BE);
            stmt.setString(2, nome);
            stmt.setDate(3, Date.valueOf(nascimento));
            stmt.setDate(4, Date.valueOf(obito));
            stmt.setString(5, causa);
            stmt.setString(6, local);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Óbito cadastrado com sucesso");
                carregarObitos();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao cadastrar óbito");
            }
            
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar óbito: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erro no Banco de Dados", 
                    "Não foi possível cadastrar o óbito: " + e.getMessage());
        }
    }
    
    private void excluirObito() {
        Obito selecionado = tableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Nenhum Item Selecionado", "Selecione um óbito para excluir");
            return;
        }
        
        try {
            String sql = "DELETE FROM obitos WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, selecionado.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Óbito excluído com sucesso");
                carregarObitos();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro", "Falha ao excluir óbito");
            }
            
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir óbito: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erro no Banco de Dados", 
                    "Não foi possível excluir o óbito: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Conexão com o banco de dados fechada");
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}