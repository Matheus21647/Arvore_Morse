package com.example.arvoremorse;



import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.IdentityHashMap;
import java.util.Map;

public class TreeVisualizer extends Application {

    private MorseBST visualTree = new MorseBST();
    private final MorseBST decoderTree = new MorseBST();

    private TextField inputField;
    private TextArea resultArea;
    private Label statusLabel;
    private Canvas canvas;

    private Button animateButton;
    private Button soundButton;
    private Button completeTreeButton;

    private Timeline animation;
    private String currentText = "";
    private String currentMorse = "";
    private int letterIndex = 0;

    private static final double RADIUS = 16;
    private static final double X_GAP = 45;
    private static final double Y_GAP = 75;
    private static final double MARGIN = 45;

    @Override
    public void start(Stage stage) {
        loadAlphabet(decoderTree);

        inputField = new TextField();
        inputField.setPromptText("Digite uma palavra");

        Button encodeButton = new Button("Codificar e animar");
        Button decodeButton = new Button("Decodificar");

        animateButton = new Button("Animar novamente");
        soundButton = new Button("Reproduzir áudio");
        completeTreeButton = new Button("Exibir árvore completa");

        encodeButton.setOnAction(e -> startInsertionAnimation());
        decodeButton.setOnAction(e -> decodeInput());
        animateButton.setOnAction(e -> startInsertionAnimation());
        soundButton.setOnAction(e -> playCurrentMorse());
        completeTreeButton.setOnAction(e -> showCompleteTree());

        HBox controls = new HBox(
                8, inputField, encodeButton, decodeButton
        );
        HBox.setHgrow(inputField, Priority.ALWAYS);

        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefRowCount(3);
        resultArea.setMaxHeight(100);

        statusLabel = new Label("Digite uma palavra para começar.");

        HBox animationControls = new HBox(
                10,
                animateButton,
                soundButton,
                completeTreeButton
        );

        canvas = new Canvas(900, 500);
        drawTree();

        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);

        VBox top = new VBox(
                10,
                controls,
                new Label("Resultado / Código Morse:"),
                resultArea,
                statusLabel
        );
        top.setPadding(new Insets(15));

        VBox center = new VBox(
                10,
                new Label(
                        "Árvore Morse: ponto = esquerda, traço = direita"
                ),
                animationControls,
                scrollPane
        );
        center.setPadding(new Insets(10));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(center);

        stage.setScene(new Scene(root, 1100, 800));
        stage.setTitle("Árvore Binária - Código Morse");
        stage.show();
    }

    private void loadAlphabet(MorseBST tree) {
        for (char c = 'A'; c <= 'Z'; c++) {
            tree.insert(c, MorseBST.getMorseCode(c));
        }

        for (char c = '0'; c <= '9'; c++) {
            tree.insert(c, MorseBST.getMorseCode(c));
        }
    }

    private void startInsertionAnimation() {
        stopAnimation();

        currentText = inputField.getText()
                .trim()
                .toUpperCase()
                .replaceAll("\\s+", " ");

        if (currentText.isEmpty()) {
            statusLabel.setText("Digite uma palavra primeiro.");
            return;
        }

        try {
            currentMorse = decoderTree.encode(currentText);
        } catch (IllegalArgumentException ex) {
            statusLabel.setText(ex.getMessage());
            return;
        }

        // Reinicia a árvore para demonstrar a inserção da entrada.
        visualTree = new MorseBST();
        letterIndex = 0;

        resizeCanvas();
        drawTree();

        resultArea.setText(
                "Texto: " + currentText
                        + "\nCódigo Morse: " + currentMorse
        );

        animateButton.setDisable(true);
        soundButton.setDisable(true);
        completeTreeButton.setDisable(true);

        animation = new Timeline(
                new KeyFrame(Duration.millis(650), event -> {
                    if (letterIndex < currentText.length()) {
                        char letter = currentText.charAt(letterIndex);

                        if (letter != ' ') {
                            String code = MorseBST.getMorseCode(letter);

                            visualTree.insert(letter, code);

                            statusLabel.setText(
                                    "Inserindo letra " + letter
                                            + " | Código Morse: " + code
                            );
                        } else {
                            statusLabel.setText("Espaço entre palavras.");
                        }

                        letterIndex++;
                        resizeCanvas();
                        drawTree();
                    }

                    if (letterIndex >= currentText.length()) {
                        animation.stop();

                        animateButton.setDisable(false);
                        soundButton.setDisable(false);
                        completeTreeButton.setDisable(false);

                        statusLabel.setText(
                                "Inserção concluída. "
                                        + "Você pode escutar o áudio."
                        );
                    }
                })
        );

        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();
    }

    private void showCompleteTree() {
        stopAnimation();

        // Carrega o alfabeto e os números na árvore visual.
        visualTree = new MorseBST();
        loadAlphabet(visualTree);

        resizeCanvas();
        drawTree();

        resultArea.setText(
                "Árvore completa carregada.\n"
                        + "Contém as letras A-Z e os números 0-9."
        );

        statusLabel.setText(
                "Exibindo a árvore completa do código Morse."
        );
    }

    private void decodeInput() {
        stopAnimation();

        String input = inputField.getText().trim();

        if (input.isEmpty()) {
            statusLabel.setText("Digite o código Morse.");
            return;
        }

        resultArea.setText(
                "Texto decodificado:\n" + decoderTree.decode(input)
        );

        statusLabel.setText("Decodificação concluída.");
    }

    private void playCurrentMorse() {
        if (currentMorse == null || currentMorse.isBlank()) {
            statusLabel.setText("Codifique um texto primeiro.");
            return;
        }

        soundButton.setDisable(true);
        statusLabel.setText("Reproduzindo código Morse...");

        Thread audioThread = new Thread(() -> {
            try {
                playMorseAudio(currentMorse);

                Platform.runLater(() -> {
                    soundButton.setDisable(false);
                    statusLabel.setText("Áudio concluído.");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    soundButton.setDisable(false);
                    statusLabel.setText(
                            "Erro ao reproduzir áudio: "
                                    + ex.getMessage()
                    );
                });
            }
        });

        audioThread.setDaemon(true);
        audioThread.start();
    }
    private void playMorseAudio(String morse) throws Exception {
        float sampleRate = 8000;

        AudioFormat format = new AudioFormat(
                sampleRate, 8, 1, true, false
        );

        SourceDataLine line = AudioSystem.getSourceDataLine(format);
        line.open(format);
        line.start();

        int dotDuration = 220;
        int dashDuration = dotDuration * 3;

        // Pausa entre os bipes da mesma letra
        int symbolGap = dotDuration;

        // Pausa extra entre letras
        int letterGap = dotDuration * 2;

        // Pausa extra entre palavras
        int wordGap = dotDuration * 5;

        try {
            for (int i = 0; i < morse.length(); i++) {
                char symbol = morse.charAt(i);

                if (symbol == '.') {
                    tone(line, sampleRate, dotDuration);
                    // silêncio escrito no áudio, não Thread.sleep
                    silence(line, sampleRate, symbolGap);

                } else if (symbol == '-') {
                    tone(line, sampleRate, dashDuration);
                    silence(line, sampleRate, symbolGap);

                } else if (symbol == '/') {
                    silence(line, sampleRate, wordGap);

                } else if (symbol == ' ') {
                    silence(line, sampleRate, letterGap);
                }
            }

            // Espera o áudio do buffer terminar de tocar
            line.drain();
        } finally {
            line.stop();
            line.close();
        }
    }

    private void tone(
            SourceDataLine line,
            float sampleRate,
            int durationMs
    ) {
        int samples = (int) (sampleRate * durationMs / 1000);
        byte[] buffer = new byte[samples];

        double frequency = 650;



        int fade = (int) (sampleRate * 0.008);

        for (int i = 0; i < samples; i++) {
            double angle =
                    2 * Math.PI * frequency * i / sampleRate;

            double envelope = 1.0;
            if (i < fade) {
                envelope = (double) i / fade;               // sobe
            } else if (i > samples - fade) {
                envelope = (double) (samples - i) / fade;   // desce
            }

            buffer[i] = (byte) (Math.sin(angle) * 110 * envelope);
        }

        line.write(buffer, 0, buffer.length);
    }

    //  amostras zeradas = silêncio  no áudio
    private void silence(
            SourceDataLine line,
            float sampleRate,
            int durationMs
    ) {
        int samples = (int) (sampleRate * durationMs / 1000);
        byte[] buffer = new byte[samples]; // byte[] já começa preenchido com 0

        line.write(buffer, 0, buffer.length);
    }


    private void resizeCanvas() {
        int count = countNodes(visualTree.getRoot());
        int height = getHeight(visualTree.getRoot());

        canvas.setWidth(
                Math.max(900, count * X_GAP + MARGIN * 2)
        );

        canvas.setHeight(
                Math.max(500, height * Y_GAP + MARGIN * 2)
        );
    }

    private int countNodes(Node node) {
        if (node == null) {
            return 0;
        }

        return 1
                + countNodes(node.getLeft())
                + countNodes(node.getRight());
    }

    private int getHeight(Node node) {
        if (node == null) {
            return 0;
        }

        return 1 + Math.max(
                getHeight(node.getLeft()),
                getHeight(node.getRight())
        );
    }

    private void drawTree() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.web("#F8FAFC"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        Map<Node, Point2D> positions = new IdentityHashMap<>();
        int[] index = {0};

        assignPositions(visualTree.getRoot(), 0, index, positions);

        gc.setStroke(Color.web("#94A3B8"));
        gc.setLineWidth(1.5);
        drawConnections(gc, visualTree.getRoot(), positions);

        drawNodes(gc, visualTree.getRoot(), positions);
    }

    private void assignPositions(
            Node node,
            int depth,
            int[] index,
            Map<Node, Point2D> positions
    ) {
        if (node == null) {
            return;
        }

        assignPositions(node.getLeft(), depth + 1, index, positions);

        double x = MARGIN + index[0] * X_GAP;
        double y = MARGIN + depth * Y_GAP;

        positions.put(node, new Point2D(x, y));
        index[0]++;

        assignPositions(node.getRight(), depth + 1, index, positions);
    }

    private void drawConnections(
            GraphicsContext gc,
            Node node,
            Map<Node, Point2D> positions
    ) {
        if (node == null) {
            return;
        }

        Point2D parent = positions.get(node);

        if (node.getLeft() != null) {
            Point2D child = positions.get(node.getLeft());

            gc.strokeLine(
                    parent.getX(), parent.getY(),
                    child.getX(), child.getY()
            );

            drawConnections(gc, node.getLeft(), positions);
        }

        if (node.getRight() != null) {
            Point2D child = positions.get(node.getRight());

            gc.strokeLine(
                    parent.getX(), parent.getY(),
                    child.getX(), child.getY()
            );

            drawConnections(gc, node.getRight(), positions);
        }
    }

    private void drawNodes(
            GraphicsContext gc,
            Node node,
            Map<Node, Point2D> positions
    ) {
        if (node == null) {
            return;
        }

        Point2D point = positions.get(node);

        gc.setFill(Color.web("#DBEAFE"));
        gc.fillOval(
                point.getX() - RADIUS,
                point.getY() - RADIUS,
                RADIUS * 2,
                RADIUS * 2
        );

        gc.setStroke(Color.web("#2563EB"));
        gc.strokeOval(
                point.getX() - RADIUS,
                point.getY() - RADIUS,
                RADIUS * 2,
                RADIUS * 2
        );

        gc.setFill(Color.web("#1E293B"));

        String label = node.getLetter() == ' '
                ? "·"
                : String.valueOf(node.getLetter());

        gc.fillText(
                label,
                point.getX() - 5,
                point.getY() + 4
        );

        drawNodes(gc, node.getLeft(), positions);
        drawNodes(gc, node.getRight(), positions);
    }

    private void stopAnimation() {
        if (animation != null) {
            animation.stop();
            animation = null;
        }

        if (animateButton != null) {
            animateButton.setDisable(false);
        }

        if (soundButton != null) {
            soundButton.setDisable(false);
        }

        if (completeTreeButton != null) {
            completeTreeButton.setDisable(false);
        }
    }

    @Override
    public void stop() {
        stopAnimation();
    }

    public static void main(String[] args) {
        launch(args);
    }
}



