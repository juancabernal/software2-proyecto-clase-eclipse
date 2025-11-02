
import java.util.List;

import co.edu.uco.ucochallenge.application.catalog.CatalogDTO;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service.orchestration.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import co.edu.uco.ucochallenge.infrastructure.primary.controller.response.ApiSuccessResponse;

@RestController
@RequestMapping("/uco-challenge/api/v1/catalogs")
public class CatalogsController {

    private final CatalogService service;

    public CatalogsController(CatalogService service) {
        this.service = service;
    }

    @GetMapping("/id-types")
    public ResponseEntity<ApiSuccessResponse<List<CatalogDTO>>> listIdTypes() {
        var data = service.listIdTypes();
        return ResponseEntity.ok(ApiSuccessResponse.of("Tipos de documento obtenidos exitosamente.", data));
    }

    @GetMapping("/cities")
    public ResponseEntity<ApiSuccessResponse<List<CatalogDTO>>> listCities() {
        var data = service.listCities();
        return ResponseEntity.ok(ApiSuccessResponse.of("Ciudades obtenidas exitosamente.", data));
    }
}
