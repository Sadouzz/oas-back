package sn.oas.facturation.features.notification.service;

public record EmailAttachment(String name, byte[] data, String mimeType) { }
