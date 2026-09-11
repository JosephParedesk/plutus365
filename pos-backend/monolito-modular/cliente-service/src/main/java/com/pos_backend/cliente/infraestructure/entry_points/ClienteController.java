package com.pos_backend.cliente.infraestructure.entry_points;

import com.pos_backend.cliente.domain.model.Cliente;
import com.pos_backend.cliente.domain.usecase.ClienteUseCase;
import com.pos_backend.cliente.infraestructure.driver_adapters.jpa_repository.ClienteData;
import com.pos_backend.cliente.infraestructure.mapper.ClienteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pos/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteUseCase clienteUseCase;
    private final ClienteMapper clienteMapper;

    // ── Listar todos (activos) ───────────────────────────────────
    @GetMapping("/listar")
    public ResponseEntity<List<Cliente>> listar(
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(clienteUseCase.listarClientes(empresaId));
    }

    // ── Buscar por id ─────────────────────────────────────────────
    @GetMapping("/buscar/{clienteId}")
    public ResponseEntity<Cliente> buscarPorId(
            @PathVariable Long clienteId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(clienteUseCase.buscarClientePorId(clienteId, empresaId));
    }

    // ── Buscar por número de documento ───────────────────────────
    @GetMapping("/documento/{numeroDocumento}")
    public ResponseEntity<Cliente> buscarPorDocumento(
            @PathVariable String numeroDocumento,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(clienteUseCase.buscarClientePorDocumento(numeroDocumento, empresaId));
    }

    // ── Crear ─────────────────────────────────────────────────────
    @PostMapping("/save")
    public ResponseEntity<Cliente> guardar(
            @RequestBody ClienteData clienteData,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        Cliente cliente = clienteMapper.toCliente(clienteData);
        return ResponseEntity.ok(clienteUseCase.guardarCliente(cliente, empresaId));
    }

    // ── Actualizar ────────────────────────────────────────────────
    @PutMapping("/actualizar/{clienteId}")
    public ResponseEntity<Cliente> actualizar(
            @PathVariable Long clienteId,
            @RequestBody ClienteData clienteData,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        Cliente cliente = clienteMapper.toCliente(clienteData);
        return ResponseEntity.ok(clienteUseCase.actualizarCliente(clienteId, cliente, empresaId));
    }

    // ── Eliminar ──────────────────────────────────────────────────
    @DeleteMapping("/eliminar/{clienteId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long clienteId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        clienteUseCase.eliminarCliente(clienteId, empresaId);
        return ResponseEntity.noContent().build();
    }
}
