package co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service.client;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.application.catalog.CatalogDTO;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.CityJpaRepository;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.IdTypeJpaRepository;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.StateJpaRepository;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.mapper.CatalogMappers;

@Service
public class CatalogServiceClient {

    private final IdTypeJpaRepository idTypeRepository;
    private final CityJpaRepository cityRepository;
    private final StateJpaRepository stateRepository;

    public CatalogServiceClient(
            IdTypeJpaRepository idTypeRepository,
            CityJpaRepository cityRepository,
            StateJpaRepository stateRepository) {
        this.idTypeRepository = idTypeRepository;
        this.cityRepository = cityRepository;
        this.stateRepository = stateRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogDTO> listIdTypes() {
        return idTypeRepository.findAllByOrderByNameAsc()
                .stream()
                .map(CatalogMappers::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogDTO> listCities() {
        return cityRepository.findAllByOrderByNameAsc()
                .stream()
                .map(CatalogMappers::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogDTO> listDepartments() {
        return stateRepository.findAllByOrderByNameAsc()
                .stream()
                .map(CatalogMappers::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogDTO> listCitiesByDepartment(String departmentId) {
        try {
            var normalizedDepartmentId = UUIDHelper.getFromString(departmentId);
            if (UUIDHelper.getDefault().equals(normalizedDepartmentId)) {
                return List.of();
            }

            return cityRepository.findAllByStateIdOrderByNameAsc(normalizedDepartmentId)
                    .stream()
                    .map(CatalogMappers::toDto)
                    .toList();
        } catch (IllegalArgumentException ex) {
            return List.of();
        }
    }
}
