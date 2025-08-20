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
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

// Classe Logger para substituir a que está faltando
class Logger {
    private static Logger instance;
    
    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }
    
    public void log(String message) {
        System.out.println("LOG: " + message);
    }
    
    public void close() {
        System.out.println("Logger fechado");
    }
}

public class App extends Application {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/cadastro";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private Logger logger;
    
    private TableView<Obito> tableView;
    private ObservableList<Obito> obitosList;
    private Connection connection;
    private SplitMenuButton localButton;

    public static class Obito {
        private int id;
        private int BE;
        private String nome_paciente;
        private LocalDate data_nascimento;
        private LocalDate dataObito;
        private String causaMorte;
        private String local_Obito;
        private String enf;
        private String leito;

        public Obito() {}

        public Obito(int BE, String nome_paciente, LocalDate data_nascimento,
                    LocalDate dataObito, String causaMorte, String local_Obito, String enf, String leito) {
            this.BE = BE;
            this.nome_paciente = nome_paciente;
            this.data_nascimento = data_nascimento;
            this.dataObito = dataObito;
            this.causaMorte = causaMorte;
            this.local_Obito = local_Obito;
            this.enf = enf;
            this.leito = leito;
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

        public String getEnf() { return enf; }
        public void setEnf(String enf) { this.enf = enf; }

        public String getLeito() { return leito; }
        public void setLeito(String leito) { this.leito = leito; }
    }

    @Override
    public void start(Stage primaryStage) {
        
        logger = Logger.getInstance();
        logger.log("Aplicação iniciada");
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

        TableColumn<Obito, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Obito, Integer> beCol = new TableColumn<>("BE");
        beCol.setCellValueFactory(new PropertyValueFactory<>("BE"));

        TableColumn<Obito, String> nomeCol = new TableColumn<>("Nome do Paciente");
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome_paciente"));

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

        TableColumn<Obito, String> enfCol = new TableColumn<>("Enfermaria");
        enfCol.setCellValueFactory(new PropertyValueFactory<>("enf"));

        TableColumn<Obito, String> leitoCol = new TableColumn<>("Leito");
        leitoCol.setCellValueFactory(new PropertyValueFactory<>("leito"));

        tableView.getColumns().addAll(idCol, beCol, nomeCol, nascimentoCol, obitoCol, localCol, causaCol, enfCol, leitoCol);

        // Configuração dos campos de formulário
        TextField beField = new TextField();
        beField.setPromptText("Número BE");
        beField.setMaxWidth(100);

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome completo");
        nomeField.setMaxWidth(250);

        DatePicker nascimentoPicker = new DatePicker();
        nascimentoPicker.setPromptText("Data nascimento");
        nascimentoPicker.setMaxWidth(120);

        DatePicker obitoPicker = new DatePicker();
        obitoPicker.setPromptText("Data óbito");
        obitoPicker.setMaxWidth(120);

        TextField causaField = new TextField();
        causaField.setPromptText("Causa da morte");
        causaField.setMaxWidth(200);

        TextField leitoField = new TextField();
        leitoField.setPromptText("Número leito");
        leitoField.setMaxWidth(100);

        localButton = new SplitMenuButton();
        localButton.setText("Selecione...");
        localButton.setMaxWidth(200);

        ObservableList<String> enfermarias = FXCollections.observableArrayList(
            "Enfermaria 1", "Enfermaria 2", "Enfermaria 3", "Enfermaria 4", "Enfermaria 5",
            "Enfermaria 6", "Enfermaria 7", "Enfermaria 8", "Enfermaria 9", "Enfermaria 10", "Enfermaria 11"
        );
        
        Spinner<String> enfSpinner = new Spinner<>();
        SpinnerValueFactory<String> valueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(enfermarias);
        enfSpinner.setValueFactory(valueFactory);
        enfSpinner.setEditable(true);
        enfSpinner.setMaxWidth(120);

        Button addButton = new Button("Adicionar");
        Button refreshButton = new Button("Atualizar Lista");
        Button deleteButton = new Button("Excluir");
        Button imprimButton = new Button("Imprimir");
        Button sairButton = new Button("Sair");

        // Configuração do conversor de data
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
                        return null;
                    }
                } else {
                    return null;
                }
            }
        };

        nascimentoPicker.setConverter(converter);
        obitoPicker.setConverter(converter);

        // Configuração do menu de local
        Menu hospitalMenu = new Menu("Hospital");
        Menu ruaMenu = new Menu("Rua");

        MenuItem hospitalAmarelo = new MenuItem("Área Amarela");
        MenuItem hospitalVermelho = new MenuItem("Área Vermelha");
        MenuItem hospitalVerde = new MenuItem("Área Verde");

        hospitalMenu.getItems().addAll(hospitalAmarelo, hospitalVermelho, hospitalVerde);

        MenuItem ruaPraia = new MenuItem("Praia");
        MenuItem ruaPraca = new MenuItem("Praça");
        MenuItem ruaAvenida = new MenuItem("Avenida");

        ruaMenu.getItems().addAll(ruaPraia, ruaPraca, ruaAvenida);

        SeparatorMenuItem separator = new SeparatorMenuItem();

        localButton.getItems().addAll(hospitalMenu, ruaMenu, separator);

        // Configuração das ações do menu
        hospitalAmarelo.setOnAction(e -> updateSelection("Hospital - Área Amarela"));
        hospitalVermelho.setOnAction(e -> updateSelection("Hospital - Área Vermelha"));
        hospitalVerde.setOnAction(e -> updateSelection("Hospital - Área Verde"));
        ruaPraia.setOnAction(e -> updateSelection("Rua - Praia"));
        ruaPraca.setOnAction(e -> updateSelection("Rua - Praça"));
        ruaAvenida.setOnAction(e -> updateSelection("Rua - Avenida"));

        // Configuração do GridPane para o formulário
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Primeira linha
        grid.add(new Label("BE:"), 0, 0);
        grid.add(beField, 1, 0);
        grid.add(new Label("Nome:"), 2, 0);
        grid.add(nomeField, 3, 0);

        // Segunda linha
        grid.add(new Label("Nascimento:"), 0, 1);
        grid.add(nascimentoPicker, 1, 1);
        grid.add(new Label("Óbito:"), 2, 1);
        grid.add(obitoPicker, 3, 1);

        // Terceira linha
        grid.add(new Label("Local Óbito:"), 0, 2);
        grid.add(localButton, 1, 2);
        grid.add(new Label("Enf...:"), 2, 2);
        grid.add(enfSpinner, 3, 2);

        // Quarta linha
        grid.add(new Label("Causa:"), 0, 3);
        grid.add(causaField, 1, 3);
        grid.add(new Label("Leito:"), 2, 3);
        grid.add(leitoField, 3, 3);

        // Quinta linha - botões
        HBox buttonsBox = new HBox(10);
        buttonsBox.getChildren().addAll(addButton, refreshButton, deleteButton, imprimButton, sairButton);
        grid.add(buttonsBox, 0, 4, 4, 1);

        // Configuração do layout principal
        VBox root = new VBox(10);
        root.getChildren().addAll(grid, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        
        Scene scene = new Scene(root, 1000, 700);

        // Configuração dos eventos dos botões
        addButton.setOnAction(e -> {
            try {
                adicionarObito(
                    Integer.parseInt(beField.getText()),
                    nomeField.getText(),
                    nascimentoPicker.getValue(),
                    obitoPicker.getValue(),
                    causaField.getText(),
                    localButton.getText(),
                    enfSpinner.getValue(),
                    leitoField.getText()
                );
                // Limpa os campos após adicionar
                beField.clear();
                nomeField.clear();
                nascimentoPicker.setValue(null);
                obitoPicker.setValue(null);
                causaField.clear();
                leitoField.clear();
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

    private void updateSelection(String text) {
        localButton.setText(text);
        System.out.println(text + " selecionado");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void carregarObitos() {
        logger.log("Iniciar o carregamento de Óbitos");
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
                obito.setEnf(rs.getString("enfermaria"));
                obito.setLeito(rs.getString("leito"));
                
                obitos.add(obito);
            }
            
            obitosList.clear();
            obitosList.addAll(obitos);
            
            logger.log("Óbitos carregados com sucesso: " + obitos.size() + " registros");
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            logger.log("Erro ao carregar óbitos: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erro no Banco de Dados",
                    "Não foi possível carregar os óbitos: " + e.getMessage());
        }
    }

    private void adicionarObito(int BE, String nome, LocalDate nascimento, LocalDate obito,
                                String causa, String local, String enf, String leito) {
        logger.log("Tentativa de adicionar óbito - BE: " + BE + ", Nome: " + nome);
        
        if (nome == null || nome.isEmpty() || nascimento == null || obito == null ||
            causa == null || causa.isEmpty() || local == null || local.isEmpty() ||
            local.equals("Selecione...")) {
            showAlert(Alert.AlertType.WARNING, "Campos Inválidos", "Todos os campos são obrigatórios");
            return;
        }
        
        try {
            String sql = "INSERT INTO obitos (BE, nome_paciente, data_nascimento, dataObito, causaMorte, local_Obito, enfermaria, leito) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, BE);
            stmt.setString(2, nome);
            stmt.setDate(3, Date.valueOf(nascimento));
            stmt.setDate(4, Date.valueOf(obito));
            stmt.setString(5, causa);
            stmt.setString(6, local);
            stmt.setString(7, enf);
            stmt.setString(8, leito);
            
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
                        "Profissional Responsável: ______________________________\n" +
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
        logger.log("Aplicação sendo encerrada");
        logger.close();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}