package com.gamescore.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "platforms",
       indexes = {
           @Index(name = "idx_platforms_slug", columnList = "slug"),
           @Index(name = "idx_platforms_name", columnList = "name")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Platform {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre de la plataforma es obligatorio")
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 100)
    private String slug;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "rawg_id", unique = true)
    private Integer rawgId;
}