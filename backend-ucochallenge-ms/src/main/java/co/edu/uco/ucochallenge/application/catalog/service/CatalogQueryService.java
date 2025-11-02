package co.edu.uco.ucochallenge.application.catalog.service;

import java.util.List;

import co.edu.uco.ucochallenge.application.catalog.dto.CatalogItemDTO;

public interface CatalogQueryService {

        List<CatalogItemDTO> listIdTypes();

        List<CatalogItemDTO> listCities();
}
