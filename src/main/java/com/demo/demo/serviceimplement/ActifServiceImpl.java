package com.demo.demo.serviceimplement;

import com.demo.demo.entities.Actif;
import com.demo.demo.repository.ActifRepository;
import com.demo.demo.services.ActifService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service

public class ActifServiceImpl  implements ActifService {
    @Autowired
    private ActifRepository actifRepository;
    @Override
    public Actif createActif(Actif actif) {
        return actifRepository.save(actif);
    }

    @Override
    public List<Actif> getAllActifs() {
        return actifRepository.findAll();

    }

    @Override
    public Optional<Actif> getActifById(Integer id) {
        return actifRepository.findById(id);
    }

    @Override
    public Actif updateActif(Integer id, Actif actifDetails) {
        Actif actif = actifRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actif not found with id: " + id));
        actif.setCodeTicket(actifDetails.getCodeTicket());
        actif.setNom(actifDetails.getNom());
        actif.setTypeActif(actifDetails.getTypeActif());
        actif.setSecteurActif(actifDetails.getSecteurActif());
        actif.setPrixNominal(actifDetails.getPrixNominal());
        actif.setVolatilite_histo(actifDetails.getVolatilite_histo());
        actif.setDescription(actifDetails.getDescription());
        actif.setPrixUpdate(actifDetails.getPrixUpdate());
        return actifRepository.save(actif);
    }
    @Override
    public void deleteActif(Integer id) {
        Actif actif = actifRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actif not found with id: " + id));
        actifRepository.delete(actif);


    }
}
