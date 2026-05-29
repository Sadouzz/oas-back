package sn.oas.facturation.mecanicien.service;

import sn.oas.facturation.mecanicien.data.entity.Mecanicien;
import sn.oas.facturation.mecanicien.dto.MecanicienRequest;

import java.util.List;
import java.util.Optional;

public interface MecanicienService {
    List<Mecanicien> getAllMecaniciens();
    Optional<Mecanicien> getMecanicienById(Long id);
    Mecanicien createMecanicien(MecanicienRequest request);
    Mecanicien updateMecanicien(Long id, MecanicienRequest request);
    void deleteMecanicien(Long id);
}
