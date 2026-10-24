package com.example.arvoremorse;


public class MorseBST {
    private final Node root = new Node(' ');

    public Node getRoot() {
        return root;
    }

    public static String getMorseCode(char letter) {
        switch (Character.toUpperCase(letter)) {
            case 'A': return ".-";
            case 'B': return "-...";
            case 'C': return "-.-.";
            case 'D': return "-..";
            case 'E': return ".";
            case 'F': return "..-.";
            case 'G': return "--.";
            case 'H': return "....";
            case 'I': return "..";
            case 'J': return ".---";
            case 'K': return "-.-";
            case 'L': return ".-..";
            case 'M': return "--";
            case 'N': return "-.";
            case 'O': return "---";
            case 'P': return ".--.";
            case 'Q': return "--.-";
            case 'R': return ".-.";
            case 'S': return "...";
            case 'T': return "-";
            case 'U': return "..-";
            case 'V': return "...-";
            case 'W': return ".--";
            case 'X': return "-..-";
            case 'Y': return "-.--";
            case 'Z': return "--..";
            case '0': return "-----";
            case '1': return ".----";
            case '2': return "..---";
            case '3': return "...--";
            case '4': return "....-";
            case '5': return ".....";
            case '6': return "-....";
            case '7': return "--...";
            case '8': return "---..";
            case '9': return "----.";
            default: return null;
        }
    }

    public void insert(char letter, String code) {
        Node current = root;

        for (char symbol : code.toCharArray()) {
            if (symbol == '.') {
                if (current.getLeft() == null) {
                    current.setLeft(new Node(' '));
                }
                current = current.getLeft();
            } else if (symbol == '-') {
                if (current.getRight() == null) {
                    current.setRight(new Node(' '));
                }
                current = current.getRight();
            }
        }

        current.setLetter(Character.toUpperCase(letter));
    }

    public String encode(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        String normalized = text.trim().toUpperCase()
                .replaceAll("\\s+", " ");

        for (char letter : normalized.toCharArray()) {
            if (letter == ' ') {
                result.append("/ ");
                continue;
            }

            String code = getMorseCode(letter);

            if (code == null) {
                throw new IllegalArgumentException(
                        "Caractere não suportado: " + letter
                );
            }

            result.append(code).append(" ");
        }

        return result.toString().trim();
    }

    public String decode(String morseText) {
        if (morseText == null || morseText.trim().isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        String[] words = morseText.trim().split("\\s*/\\s*");

        for (int w = 0; w < words.length; w++) {
            String word = words[w].trim();

            if (!word.isEmpty()) {
                for (String code : word.split("\\s+")) {
                    result.append(decodeCode(code));
                }
            }

            if (w < words.length - 1) {
                result.append(' ');
            }
        }

        return result.toString();
    }

    private char decodeCode(String code) {
        Node current = root;

        for (char symbol : code.toCharArray()) {
            if (symbol == '.') {
                current = current.getLeft();
            } else if (symbol == '-') {
                current = current.getRight();
            } else {
                return '?';
            }

            if (current == null) {
                return '?';
            }
        }

        return current.getLetter() == ' ' ? '?' : current.getLetter();
    }
}

