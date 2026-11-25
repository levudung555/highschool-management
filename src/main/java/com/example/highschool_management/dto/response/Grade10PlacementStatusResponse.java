package com.example.highschool_management.dto.response;

import com.example.highschool_management.model.SchoolClass;
import java.time.LocalDate;
import java.util.List;

public record Grade10PlacementStatusResponse(
        LocalDate ngayVaoLopDaChot,
        List<SchoolClass> lopDaChuanBi
) {}
