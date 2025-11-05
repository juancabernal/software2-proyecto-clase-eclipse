package co.edu.uco.ucochallenge.infrastructure.secondary.repository.mapper;


import co.edu.uco.ucochallenge.application.catalog.CatalogDTO;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity.CityEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity.IdTypeEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity.StateEntity;

public final class CatalogMappers {
    private CatalogMappers() {}

    public static CatalogDTO toDto(IdTypeEntity e) {
        return new CatalogDTO(e.getId(), e.getName());
    }

    public static CatalogDTO toDto(CityEntity e) {
        return new CatalogDTO(e.getId(), e.getName());
    }

    public static CatalogDTO toDto(StateEntity e) {
        return new CatalogDTO(e.getId(), e.getName());
    }
}
