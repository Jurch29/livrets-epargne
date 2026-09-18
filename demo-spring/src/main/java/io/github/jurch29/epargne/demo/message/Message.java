package io.github.jurch29.epargne.demo.message;

/** Donnée métier, immuable. Jackson la sérialise en JSON sans configuration. */
public record Message(long id, String texte) {}
