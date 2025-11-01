package co.edu.uco.ucochallenge.application.user.updateUser.dto;

import java.util.List;
import java.util.UUID;

import co.edu.uco.ucochallenge.application.hateoas.LinkDTO;

public record UpdateUserOutputDTO(
                UUID userId,
                String fullName,
                String email,
                List<LinkDTO> links) {

        public static UpdateUserOutputDTO of(
                        final UUID userId,
                        final String fullName,
                        final String email,
                        final List<LinkDTO> links) {
                return new UpdateUserOutputDTO(userId, fullName, email, links);
        }
}
