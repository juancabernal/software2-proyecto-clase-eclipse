package co.edu.uco.ucochallenge.infrastructure.primary.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uco.ucochallenge.application.catalog.dto.CatalogItemDTO;
import co.edu.uco.ucochallenge.application.catalog.service.CatalogQueryService;
import co.edu.uco.ucochallenge.infrastructure.primary.controller.response.ApiSuccessResponse;

@RestController
@RequestMapping("/uco-challenge/api/v1/catalogs")
public class CatalogController {

        private final CatalogQueryService catalogQueryService;

        public CatalogController(final CatalogQueryService catalogQueryService) {
                this.catalogQueryService = catalogQueryService;
        }

        @GetMapping("/id-types")
        public ResponseEntity<ApiSuccessResponse<List<CatalogItemDTO>>> listIdTypes() {
                final var data = catalogQueryService.listIdTypes();
                return ResponseEntity.ok(ApiSuccessResponse.of("Tipos de identificación obtenidos exitosamente.", data));
        }

        @GetMapping("/departments")
        public ResponseEntity<ApiSuccessResponse<List<CatalogItemDTO>>> listDepartments() {
                final var data = catalogQueryService.listDepartments();
                return ResponseEntity.ok(ApiSuccessResponse.of("Departamentos obtenidos exitosamente.", data));
        }

        @GetMapping("/cities")
        public ResponseEntity<ApiSuccessResponse<List<CatalogItemDTO>>> listCities(
                        @RequestParam(required = false) final UUID departmentId) {
                final var data = catalogQueryService.listCities(departmentId);
                return ResponseEntity.ok(ApiSuccessResponse.of("Ciudades obtenidas exitosamente.", data));
        }

        @GetMapping("/departments/{departmentId}/cities")
        public ResponseEntity<ApiSuccessResponse<List<CatalogItemDTO>>> listCitiesByDepartment(
                        @PathVariable final UUID departmentId) {
                final var data = catalogQueryService.listCitiesByDepartment(departmentId);
                return ResponseEntity.ok(ApiSuccessResponse.of("Ciudades obtenidas exitosamente.", data));
        }
}
