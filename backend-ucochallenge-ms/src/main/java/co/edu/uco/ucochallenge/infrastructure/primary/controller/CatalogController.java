package co.edu.uco.ucochallenge.infrastructure.primary.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
                final List<CatalogItemDTO> response = catalogQueryService.listIdTypes();
                return ResponseEntity.ok(ApiSuccessResponse.of("Tipos de identificación obtenidos exitosamente.", response));
        }

        @GetMapping("/cities")
        public ResponseEntity<ApiSuccessResponse<List<CatalogItemDTO>>> listCities() {
                final List<CatalogItemDTO> response = catalogQueryService.listCities();
                return ResponseEntity.ok(ApiSuccessResponse.of("Ciudades obtenidas exitosamente.", response));
        }
}
