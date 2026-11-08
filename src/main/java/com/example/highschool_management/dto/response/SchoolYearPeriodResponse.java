package com.example.highschool_management.dto.response;

import java.time.LocalDate;


public class SchoolYearPeriodResponse {

    private final LocalDate batDau;
    private final LocalDate ketThuc;

    public SchoolYearPeriodResponse(
            LocalDate batDau,
            LocalDate ketThuc) {
        this.batDau = batDau;
        this.ketThuc = ketThuc;
    }

    public LocalDate getBatDau() {
        return batDau;
    }

    public LocalDate getKetThuc() {
        return ketThuc;
    }
}