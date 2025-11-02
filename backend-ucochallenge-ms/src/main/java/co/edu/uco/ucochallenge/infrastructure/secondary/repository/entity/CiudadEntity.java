package co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity;

import java.util.UUID;

import co.edu.uco.ucochallenge.crosscuting.helper.ObjectHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ciudad")
public class CiudadEntity {

        @Id
        @Column(name = "id")
        private UUID id;

        @ManyToOne
        @JoinColumn(name = "departamento")
        private DepartamentoEntity departamento;

        @Column(name = "nombre")
        private String nombre;

        protected CiudadEntity() {
                setId(UUIDHelper.getDefault());
                setDepartamento(new DepartamentoEntity());
                setNombre(TextHelper.getDefault());
        }

        private CiudadEntity(final Builder builder) {
                setId(builder.id);
                setDepartamento(builder.departamento);
                setNombre(builder.nombre);
        }

        public static class Builder {
                private UUID id;
                private DepartamentoEntity departamento;
                private String nombre;

                public Builder id(final UUID id) {
                        this.id = id;
                        return this;
                }

                public Builder departamento(final DepartamentoEntity departamento) {
                        this.departamento = departamento;
                        return this;
                }

                public Builder nombre(final String nombre) {
                        this.nombre = nombre;
                        return this;
                }

                public CiudadEntity build() {
                        return new CiudadEntity(this);
                }
        }

        public UUID getId() {
                return id;
        }

        public DepartamentoEntity getDepartamento() {
                return departamento;
        }

        public String getNombre() {
                return nombre;
        }

        private void setId(final UUID id) {
                this.id = UUIDHelper.getDefault(id);
        }

        private void setDepartamento(final DepartamentoEntity departamento) {
                this.departamento = ObjectHelper.getDefault(departamento, new DepartamentoEntity());
        }

        private void setNombre(final String nombre) {
                this.nombre = TextHelper.getDefaultWithTrim(nombre);
        }
}
