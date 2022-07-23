package com.example.demo.controller;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class EntityDTO {
    private String value;
    private String rango;
    private ObjetDto objet;
}
