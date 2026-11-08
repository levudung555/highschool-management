package com.example.highschool_management.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {
    Long id;            // MaVT
    String name;        // MaSoVaiTro (e.g. ADMIN, TEACHER, STUDENT)
    String description; // TenVaiTro

    public Role(String name) {
        this.name = name;
    }

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Role(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
