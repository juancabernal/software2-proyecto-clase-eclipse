package co.edu.uco.ucochallenge.infrastructure.secondary.service.catalog;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.application.catalog.dto.CatalogItemDTO;
import co.edu.uco.ucochallenge.application.catalog.service.CatalogQueryService;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.CityJpaRepository;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.IdTypeJpaRepository;

@Service
@Transactional(readOnly = true)
public class CatalogQueryServiceImpl implements CatalogQueryService {

        private final IdTypeJpaRepository idTypeJpaRepository;
        private final CityJpaRepository cityJpaRepository;

        public CatalogQueryServiceImpl(final IdTypeJpaRepository idTypeJpaRepository,
                        final CityJpaRepository cityJpaRepository) {
                this.idTypeJpaRepository = idTypeJpaRepository;
                this.cityJpaRepository = cityJpaRepository;
        }

        @Override
        public List<CatalogItemDTO> listIdTypes() {
                return idTypeJpaRepository.findAll().stream()
                                .sorted(Comparator.comparing(entity -> entity.getName().toLowerCase()))
                                .map(entity -> new CatalogItemDTO(entity.getId(), entity.getName()))
                                .collect(Collectors.toList());
        }

        @Override
        public List<CatalogItemDTO> listCities() {
                return cityJpaRepository.findAll().stream()
                                .sorted(Comparator.comparing(entity -> entity.getName().toLowerCase()))
                                .map(entity -> new CatalogItemDTO(entity.getId(), entity.getName()))
                                .collect(Collectors.toList());
        }
}
