package org.example.studyos.contenu.extraction;

import java.util.List;

/**
 * Decoupe le XHTML de Tika en sections. Une implementation par famille de format,
 * choisie par type MIME dans FabriqueSectionneur (meme motif que SourceAgenda).
 */
public interface SectionneurDocument {

    List<Section> decouper(String xhtmlTika);
}
