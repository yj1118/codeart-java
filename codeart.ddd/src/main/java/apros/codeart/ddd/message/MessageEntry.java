package apros.codeart.ddd.message;

import apros.codeart.dto.DTObject;

import java.util.UUID;

public record MessageEntry(String name, String id, DTObject content) {

}
