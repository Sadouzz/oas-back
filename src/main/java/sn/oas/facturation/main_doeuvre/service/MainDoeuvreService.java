package sn.oas.facturation.main_doeuvre.service;

import sn.oas.facturation.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.main_doeuvre.dto.MainDoeuvreRequest;

import java.util.List;

public interface MainDoeuvreService {
    List<MainDoeuvre> getAllMainDoeuvres();
    MainDoeuvre getMainDoeuvreById(Long id);
    MainDoeuvre createMainDoeuvre(MainDoeuvreRequest request);
    MainDoeuvre updateMainDoeuvre(Long id, MainDoeuvreRequest request);
    void deleteMainDoeuvre(Long id);
    MainDoeuvre archiveMainDoeuvre(Long id, boolean archived);
}