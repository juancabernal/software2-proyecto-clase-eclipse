package co.edu.uco.api_gateway.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

public record VerificationCodeRequestDto(@JsonAlias({"token"}) UUID tokenId, String code) {
}
