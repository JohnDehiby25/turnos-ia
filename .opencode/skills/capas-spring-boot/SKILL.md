---
name: capas-spring-boot
description: Convenciones del backend Spring Boot en capas (controller, service, repository, domain, dto). Cargar al crear o modificar código Java.
---

# Capas Spring Boot

- Dependencias solo hacia abajo: controller → service → repository → domain.
- Controllers: reciben/devuelven DTOs (`record`), `@Valid`, sin lógica. Nunca exponer entidades JPA.
- Services: `@Transactional`, reglas de negocio, lanzan excepciones de dominio (`BusinessRuleException`).
- Repositories: interfaces Spring Data; consultas complejas con `@Query`.
- Manejo de errores central con `@RestControllerAdvice` → `{ code, message }`.
- Inyección por constructor; sin `@Autowired` en campos.
- Nombres: `XxxController`, `XxxService`, `XxxRepository`, `XxxRequest/Response`.
- Tests: unitarios de services (Mockito) y de integración con Testcontainers (PostgreSQL).
- Herramientas del bot (`ai/tools`) llaman a services, nunca a repositories.
