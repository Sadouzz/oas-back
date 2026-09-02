package sn.oas.facturation.features.main_doeuvre.service;

import sn.oas.facturation.features.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.features.main_doeuvre.dto.MainDoeuvreRequest;

import org.springframework.data.domain.Page;
import java.util.List;

public interface MainDoeuvreService {
    Page<MainDoeuvre> getAllMainDoeuvres(int page, int size);
    List<MainDoeuvre> getAllMainDoeuvres();
    MainDoeuvre getMainDoeuvreById(Long id);
    MainDoeuvre createMainDoeuvre(MainDoeuvreRequest request);
    MainDoeuvre updateMainDoeuvre(Long id, MainDoeuvreRequest request);
    void deleteMainDoeuvre(Long id);
    MainDoeuvre archiveMainDoeuvre(Long id, boolean archived);
}