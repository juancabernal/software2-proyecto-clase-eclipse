package co.edu.uco.ucochallenge.application.catalog.service;

import java.util.List;
import java.util.UUID;

import co.edu.uco.ucochallenge.application.catalog.dto.CatalogItemDTO;

public interface CatalogQueryService {

        List<CatalogItemDTO> listIdTypes();

        List<CatalogItemDTO> listDepartments();

        List<CatalogItemDTO> listCities(UUID departmentId);

        List<CatalogItemDTO> listCitiesByDepartment(UUID departmentId);
}
