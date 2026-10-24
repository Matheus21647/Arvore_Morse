package com.example.arvoremorse;

public class Node {
    private char letter;
    private Node left;
    private Node right;

    public Node(char letter) {
        this.letter = letter;
        this.left = null;
        this.right = null;
    }

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }
}


