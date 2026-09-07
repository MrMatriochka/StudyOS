package org.example.studyos.agenda.web;

import org.example.studyos.agenda.ingestion.ResultatImport;
import org.example.studyos.agenda.service.ServiceAgenda;
import org.example.studyos.agenda.service.ServiceImport;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@RestController
@RequestMapping("/api/imports")
public class ControleurImport {

    private final ServiceImport serviceImport;
    private final ServiceAgenda serviceAgenda;

    public ControleurImport(ServiceImport serviceImport, ServiceAgenda serviceAgenda) {
        this.serviceImport = serviceImport;
        this.serviceAgenda = serviceAgenda;
    }

    /** Import d'un .ics (multipart, champ « fichier »). */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultatImport importer(@RequestParam("fichier") MultipartFile fichier) {
        try {
            return serviceImport.importer(fichier.getOriginalFilename(), fichier.getInputStream());
        } catch (IOException e) {
            throw new UncheckedIOException("Lecture du fichier importe impossible", e);
        }
    }

    /** Historique des imports, du plus recent au plus ancien. */
    @GetMapping
    public List<ImportDTO> historique() {
        return serviceAgenda.historiqueImports().stream().map(ImportDTO::de).toList();
    }
}
