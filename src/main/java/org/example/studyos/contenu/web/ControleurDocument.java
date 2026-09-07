package org.example.studyos.contenu.web;

import org.example.studyos.agenda.web.SeanceDTO;
import org.example.studyos.contenu.domaine.Document;
import org.example.studyos.contenu.service.ServiceDocument;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class ControleurDocument {

    private final ServiceDocument service;

    public ControleurDocument(ServiceDocument service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultatDepotDTO deposer(@RequestParam("fichier") MultipartFile fichier,
                                    @RequestParam UUID matiereId,
                                    @RequestParam(required = false) UUID seanceId,
                                    @RequestParam(required = false) String titre) {
        try {
            return ResultatDepotDTO.de(service.deposer(
                    matiereId, seanceId, titre, fichier.getOriginalFilename(), fichier.getBytes()));
        } catch (IOException e) {
            throw new UncheckedIOException("Lecture du fichier depose impossible", e);
        }
    }

    @GetMapping
    public List<DocumentDTO> lister(@RequestParam UUID matiereId,
                                    @RequestParam(required = false) UUID seanceId) {
        return service.lister(matiereId, seanceId).stream().map(DocumentDTO::de).toList();
    }

    @GetMapping("/{id}")
    public DocumentDTO parId(@PathVariable UUID id) {
        return DocumentDTO.de(service.parId(id));
    }

    @GetMapping("/{id}/sections")
    public List<SectionDTO> sections(@PathVariable UUID id) {
        return service.sections(id).stream().map(SectionDTO::de).toList();
    }

    @GetMapping("/{id}/fichier")
    public ResponseEntity<byte[]> fichier(@PathVariable UUID id) {
        Document document = service.parId(id);
        byte[] octets = service.octets(document);
        return ResponseEntity.ok()
                .contentType(typeMedia(document.getExtension()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(document.getNomOriginal()).build().toString())
                .body(octets);
    }

    @PatchMapping("/{id}")
    public DocumentDTO mettreAJour(@PathVariable UUID id, @RequestBody MajDocumentDTO corps) {
        return DocumentDTO.de(service.mettreAJour(
                id, corps.titre(), corps.type(), corps.seanceId(), corps.detacherSeance()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }

    /** Pre-remplissage : seance proposee (la plus proche, +/-10 j). 204 si aucune. */
    @GetMapping("/proposition-seance")
    public ResponseEntity<SeanceDTO> propositionSeance(@RequestParam UUID matiereId) {
        return service.proposerSeance(matiereId, LocalDate.now(ZoneId.of("Europe/Paris")))
                .map(SeanceDTO::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private static MediaType typeMedia(String extension) {
        return switch (extension) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "txt", "md" -> MediaType.TEXT_PLAIN;
            case "png" -> MediaType.IMAGE_PNG;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
