package com.demo.demo.services;

import com.demo.demo.entities.Actif;

import java.util.List;
import java.util.Optional;

public interface ActifService {
    Actif createActif(Actif actif);
    List<Actif> getAllActifs();
    Optional<Actif> getActifById(Integer id);
    Actif updateActif(Integer id, Actif actifDetails);
    void deleteActif(Integer id);
}
