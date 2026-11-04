package com.example.highschool_management.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Getter
@Setter
public class Guardian {
    Long maNGH;
    String hoTen;
    String sdt;
    String email;
    String diaChi;
}
