package com.pos_backend.app.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Reglas obligatorias de CLAUDE.md / MIGRACION-MONOLITO-MODULAR.md, verificadas
 * automáticamente en cada build en vez de a mano o de memoria.
 *
 * A medida que entren módulos migrados (categoria, proveedor, ...) hay que sumar
 * acá la regla de límites entre módulos: ningún módulo puede importar
 * infraestructure.driver_adapters.jpa_repository.* ni .mapper.* de otro módulo.
 * No se escribe todavía porque con un solo módulo (app, vacío) no hay forma de
 * verificarla contra código real.
 */
class ArchitectureRulesTest {

    private static final com.tngtech.archunit.core.domain.JavaClasses CLASES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.pos_backend");

    @Test
    void elDominioNoDependeDeSpring() {
        // allowEmptyShould: hoy `app` no tiene ninguna clase de dominio todavía
        // (no hay módulos migrados). En cuanto entre el primero, esta regla
        // empieza a verificar contra código real.
        ArchRule regla = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..")
                .allowEmptyShould(true);

        regla.check(CLASES);
    }

    @Test
    void losUseCaseNoSeInstancianComoBeanDeSpring() {
        ArchRule regla = noClasses()
                .that().resideInAPackage("..domain.usecase..")
                .should().beAnnotatedWith(Service.class)
                .orShould().beAnnotatedWith(Component.class)
                .allowEmptyShould(true);

        regla.check(CLASES);
    }
}
