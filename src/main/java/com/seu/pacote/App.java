package com.seu.pacote;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.print.PrinterJob;
import javafx.scene.control.TextArea;

public class App extends Application {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/cadastro";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private Logger logger;
    
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

        logger = Logger.getInstance();
        logger.log("Aplicaçao iniciada");
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            logger.log("Conexão com o banco de dados estabelecida com sucesso!");
        } catch (SQLException e) {
            logger.log("Erro ao conectar ao banco de dados: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erro de Conexão",
                    "Não foi possível conectar ao banco de dados: " + e.getMessage());
            return;
        }

        tableView = new TableView<>();
        obitosList = FXCollections.observableArrayList();
        tableView.setItems(obitosList);

        tableView.getItems().addListener((ListChangeListener<Obito>)c -> {
            System.out.println("TableView items changed. Total: " + c.getList().size());
        });

        TableColumn<Obito, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Obito, Integer> beCol = new TableColumn<>("BE");
        beCol.setCellValueFactory(new PropertyValueFactory<>("BE"));

        TableColumn<Obito, String> nomeCol = new TableColumn<>("Nome do Paciente");
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome_paciente"));

        // Coluna de Data de Nascimento - corrigida
        TableColumn<Obito, LocalDate> nascimentoCol = new TableColumn<>("Data de Nascimento");
        nascimentoCol.setCellValueFactory(new PropertyValueFactory<>("data_nascimento"));
        nascimentoCol.setCellFactory(column -> new TableCell<Obito, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        // Coluna de Data de Óbito - corrigida
        TableColumn<Obito, LocalDate> obitoCol = new TableColumn<>("Data Óbito");
        obitoCol.setCellValueFactory(new PropertyValueFactory<>("dataObito"));
        obitoCol.setCellFactory(column -> new TableCell<Obito, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
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
        Button imprimButton = new Button("Imprimir");
        Button sairButton = new Button("Sair");

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
        buttonsPane.addRow(0, addButton, refreshButton, deleteButton, imprimButton,sairButton);
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
        imprimButton.setOnAction(e -> Imprimir());
        sairButton.setOnAction(e -> Platform.exit());

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
        logger.log("Iniciar o carregamento de Obito");
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
                
                Date dataNasc = rs.getDate("data_nascimento");
                obito.setData_nascimento(dataNasc != null ? dataNasc.toLocalDate() : null);
                
                Date dataObito = rs.getDate("dataObito");
                obito.setDataObito(dataObito != null ? dataObito.toLocalDate() : null);
                
                obito.setCausaMorte(rs.getString("causaMorte"));
                obito.setLocal_Obito(rs.getString("local_Obito"));
                obitos.add(obito);
            }
            
            // Limpa e atualiza a lista observável
            obitosList.clear();
            obitosList.addAll(obitos);
            
            // Verificação de debug
            System.out.println("Total de óbitos carregados: " + obitos.size());
            for (Obito o : obitos) {
                System.out.println(o.getId() + " - " + o.getNome_paciente());
            }
            
            logger.log("Óbitos carregados com sucesso: " + obitos.size() + " registros");
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            logger.log("Erro ao carregar óbitos: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erro no Banco de Dados",
                    "Não foi possível carregar os óbitos: " + e.getMessage());
        }
    }

    private void adicionarObito(int BE, String nome, LocalDate nascimento, LocalDate obito, String causa, String local) {
        logger.log("Tentativa de adicionar obito - BE: " + BE + ", Nome" + nome);
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
            logger.log("Erro ao cadastrar óbito: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erro no Banco de Dados",
                    "Não foi possível cadastrar o óbito: " + e.getMessage());
        }
    }
    
    private void excluirObito() {
        Obito selecionado = tableView.getSelectionModel().getSelectedItem();
        logger.log("Tentativa de excluir óbito - ID: " + (selecionado != null ? selecionado.getId() : "nenhum selecionado"));
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

    private void Imprimir(){
        Obito selecionado = tableView.getSelectionModel().getSelectedItem();
        if(selecionado == null){
            showAlert(Alert.AlertType.WARNING, "Nenhum Item Selecionado", "Selecione um Óbito para imprimir");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dataNasc = selecionado.getData_nascimento() != null ? selecionado.getData_nascimento().format(formatter) : "___/___/___";
        String dataObito = selecionado.getDataObito() != null ? selecionado.getDataObito().format(formatter) : "___/___/___";

        //Criar o modelo preenchido
        String modelo = "NOTIFICAÇÃO DE ÓBITO - SINAN\n\n" +
                        "Dados do Paciente:\n" +
                        "Nome: " + (selecionado.getNome_paciente() != null ? selecionado.getNome_paciente() : "") + "\n" +
                        "Data de Nascimento: " + dataNasc + "  Sexo: □ M □ F\n" +
                        "Nome da Mãe: ________________________________________\n" +
                        "Dados do Óbito:\n" +
                        "Data do Óbito: " + dataObito + "  Hora: __:__\n" +
                        "Local do Óbito: " + (selecionado.getLocal_Obito() != null ? selecionado.getLocal_Obito() : "") + "\n" +
                        "Causa Básica do Óbito (CID-10): " + (selecionado.getCausaMorte() != null ? selecionado.getCausaMorte() : "") + "\n\n" +
                        "Dados de Notificação:\n" +
                        "Unidade de Saúde Notificante: _________________________\n" +
                        "Proficional Responsável: ______________________________\n" +
                        "Data de Notificação: __/___/__";
        
        TextArea textArea = new TextArea(modelo);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 14px;");
        
        Stage printStage = new Stage();
        printStage.setTitle("Impressão Óbito");
        VBox root = new VBox(20, textArea);
        root.setPadding(new Insets(15));

        Button printButton = new Button("Imprimir");
        printButton.setOnAction(e -> {
            // Aqui você pode implementar a impressão real usando PrinterJob
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(printStage)) {
                boolean success = job.printPage(textArea);
                if (success) {
                    job.endJob();
                }
            }
        });
        
        root.getChildren().add(printButton);
        
        printStage.setScene(new Scene(root, 600, 500));
        printStage.show();
    }
    @Override
    public void stop() throws Exception {
        logger.log("Aplicação sendo encerada");
        logger.close();
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