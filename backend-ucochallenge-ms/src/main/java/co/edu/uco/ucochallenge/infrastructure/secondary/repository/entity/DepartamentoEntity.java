package co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity;

import java.util.UUID;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "departamento")
public class DepartamentoEntity {

        @Id
        @Column(name = "id")
        private UUID id;

        @Column(name = "nombre")
        private String nombre;

        protected DepartamentoEntity() {
                setId(UUIDHelper.getDefault());
                setNombre(TextHelper.getDefault());
        }

        private DepartamentoEntity(final Builder builder) {
                setId(builder.id);
                setNombre(builder.nombre);
        }

        public static final class Builder {
                private UUID id;
                private String nombre;

                public Builder id(final UUID id) {
                        this.id = id;
                        return this;
                }

                public Builder nombre(final String nombre) {
                        this.nombre = nombre;
                        return this;
                }

                public DepartamentoEntity build() {
                        return new DepartamentoEntity(this);
                }
        }

        public UUID getId() {
                return id;
        }

        public String getNombre() {
                return nombre;
        }

        private void setId(final UUID id) {
                this.id = UUIDHelper.getDefault(id);
        }

        private void setNombre(final String nombre) {
                this.nombre = TextHelper.getDefaultWithTrim(nombre);
        }
}
