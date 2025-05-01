package org.project.group5.gamelend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name = "";

    @Column(nullable = false, unique = true, length = 100)
    private String fileName = "";

    @Column(nullable = false, length = 10)
    private String extension = "";

    @Column(nullable = false, length = 1)
    private String status = "D";

    @Column(nullable = false)
    private boolean deleted = false;

    @Lob
    private byte[] image;

    @Column(length = 255)
    private String localPath;

    @Transient
    private String urlFile;

    public String getCompleteFileName() {
        return fileName + extension;
    }
}