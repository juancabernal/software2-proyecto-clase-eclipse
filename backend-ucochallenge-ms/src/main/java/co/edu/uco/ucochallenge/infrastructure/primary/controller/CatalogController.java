package co.edu.uco.ucochallenge.infrastructure.primary.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uco.ucochallenge.application.catalog.dto.CatalogItemDTO;
import co.edu.uco.ucochallenge.application.catalog.service.CatalogQueryService;

@RestController
@RequestMapping("/uco-challenge/api/v1/catalogs")
public class CatalogController {

        private final CatalogQueryService catalogQueryService;

        public CatalogController(final CatalogQueryService catalogQueryService) {
                this.catalogQueryService = catalogQueryService;
        }

        @GetMapping("/id-types")
        public ResponseEntity<List<CatalogItemDTO>> listIdTypes() {
                return ResponseEntity.ok(catalogQueryService.listIdTypes());
        }

        @GetMapping("/departments")
        public ResponseEntity<List<CatalogItemDTO>> listDepartments() {
                return ResponseEntity.ok(catalogQueryService.listDepartments());
        }

        @GetMapping("/cities")
        public ResponseEntity<List<CatalogItemDTO>> listCities(@RequestParam(required = false) final UUID departmentId) {
                return ResponseEntity.ok(catalogQueryService.listCities(departmentId));
        }
}
