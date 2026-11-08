package com.example.highschool_management.model;

import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    Long id;          // MaTK
    Long maGV;        // MaGV
    String username;  // TenDangNhap
    String password;  // MatKhauBam
    String fullName;  // HoTen
    String email;     // Email
    @Builder.Default
    boolean enabled = true;
    @Builder.Default
    String trangThai = "HOAT_DONG";
    @Builder.Default
    Integer tokenVersion = 0;
    @Builder.Default
    Set<Role> roles = new HashSet<>();
}
