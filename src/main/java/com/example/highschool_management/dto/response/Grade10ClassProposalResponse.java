package com.example.highschool_management.dto.response;

public record Grade10ClassProposalResponse(
        Long maToHop,
        String kyHieu,
        long tongNguyenVong,
        long soLopDaChuanBi,
        long tongSucChua,
        long soChoConThieu,
        long soLopCanMoThem
) {}