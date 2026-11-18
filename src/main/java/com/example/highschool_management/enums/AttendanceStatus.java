package com.example.highschool_management.enums;

/**
 * Trạng thái điểm danh học sinh theo đặc tả UC11
 */
public enum AttendanceStatus {
    CO_MAT("Có mặt"),
    VANG_CO_PHEP("Vắng có phép"),
    VANG_KHONG_PHEP("Vắng không phép"),
    DI_MUON("Đi muộn");

    private final String moTa;

    AttendanceStatus(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }
}
