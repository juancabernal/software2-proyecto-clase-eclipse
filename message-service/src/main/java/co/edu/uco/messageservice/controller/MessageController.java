package co.edu.uco.messageservice.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uco.messageservice.catalog.MessageCatalog;
import co.edu.uco.messageservice.catalog.MessageEntry;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/messages/api/v1/messages")
@Validated
public class MessageController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, MessageEntry>> getAllMessages() {
        return ResponseEntity.ok(MessageCatalog.getAllMessages());
    }

    @GetMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageEntry> getMessage(@PathVariable String code) {
        MessageEntry entry = MessageCatalog.getMessage(code);
        if (entry == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(entry);
    }

    @PutMapping(path = "/{code}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageEntry> upsertMessage(@PathVariable String code, @Valid @RequestBody MessageEntry body) {
        MessageEntry updated = MessageCatalog.upsertMessage(code, body);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageEntry> deleteMessage(@PathVariable String code) {
        MessageEntry removed = MessageCatalog.removeMessage(code);
        if (removed == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(removed);
    }
}
