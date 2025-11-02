package co.edu.uco.ucochallenge.infrastructure.secondary.service.catalog;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.application.catalog.dto.CatalogItemDTO;
import co.edu.uco.ucochallenge.application.catalog.service.CatalogQueryService;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.CityJpaRepository;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.DepartmentJpaRepository;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.IdTypeJpaRepository;

@Service
@Transactional(readOnly = true)
public class CatalogQueryServiceImpl implements CatalogQueryService {

        private final IdTypeJpaRepository idTypeJpaRepository;
        private final DepartmentJpaRepository departmentJpaRepository;
        private final CityJpaRepository cityJpaRepository;

        public CatalogQueryServiceImpl(final IdTypeJpaRepository idTypeJpaRepository,
                        final DepartmentJpaRepository departmentJpaRepository,
                        final CityJpaRepository cityJpaRepository) {
                this.idTypeJpaRepository = idTypeJpaRepository;
                this.departmentJpaRepository = departmentJpaRepository;
                this.cityJpaRepository = cityJpaRepository;
        }

        @Override
        public List<CatalogItemDTO> listIdTypes() {
                return idTypeJpaRepository.findAll().stream()
                                .sorted(Comparator.comparing(entity -> entity.getNombre().toLowerCase()))
                                .map(entity -> new CatalogItemDTO(entity.getId(), entity.getNombre()))
                                .collect(Collectors.toList());
        }

        @Override
        public List<CatalogItemDTO> listDepartments() {
                return departmentJpaRepository.findAll().stream()
                                .sorted(Comparator.comparing(entity -> entity.getNombre().toLowerCase()))
                                .map(entity -> new CatalogItemDTO(entity.getId(), entity.getNombre()))
                                .collect(Collectors.toList());
        }

        @Override
        public List<CatalogItemDTO> listCities(final UUID departmentId) {
                return (departmentId != null ? cityJpaRepository.findByDepartamento_Id(departmentId)
                                : cityJpaRepository.findAll()).stream()
                                .sorted(Comparator.comparing(entity -> entity.getNombre().toLowerCase()))
                                .map(entity -> new CatalogItemDTO(entity.getId(), entity.getNombre()))
                                .collect(Collectors.toList());
        }
}
