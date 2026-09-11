package com.pos_backend.subscription_service.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.subscription_service.domain.model.Suscripcion;
import com.pos_backend.subscription_service.domain.model.gateway.SuscripcionGateway;
import com.pos_backend.subscription_service.infraestructure.mapper.SuscripcionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SuscripcionDataGatewayImpl implements SuscripcionGateway {

    private final SuscripcionDataJpaRepository suscripcionDataJpaRepository;
    private final SuscripcionMapper suscripcionMapper;

    @Override
    public Suscripcion guardarSuscripcion(Suscripcion suscripcion) {
        SuscripcionData saved = suscripcionDataJpaRepository
                .save(suscripcionMapper.toSuscripcionData(suscripcion));
        return suscripcionMapper.toSuscripcion(saved);
    }

    @Override
    public Suscripcion buscarPorUsuario(String usuarioCedula) {
        return suscripcionDataJpaRepository.findByUsuarioCedula(usuarioCedula)
                .map(suscripcionMapper::toSuscripcion).orElse(null);
    }

    @Override
    public Suscripcion buscarPorId(Long id) {
        return suscripcionDataJpaRepository.findById(id)
                .map(suscripcionMapper::toSuscripcion).orElse(null);
    }

    @Override
    public List<Suscripcion> listarTodas() {
        return suscripcionDataJpaRepository.findAll()
                .stream().map(suscripcionMapper::toSuscripcion).toList();
    }

    @Override
    public void cancelarSuscripcion(Long id) {
        suscripcionDataJpaRepository.deleteById(id);
    }
}