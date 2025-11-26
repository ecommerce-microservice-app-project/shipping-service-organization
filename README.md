# Shipping Service

Shipping Service para sistema de ecommerce. Gestiona los items de orden (order items) que representan los productos incluidos en cada orden de compra.

## Características

- Spring Boot 2.5.7 con Java 11
- Base de datos: H2 (dev) / MySQL (stage/prod)
- Service Discovery: Eureka Client
- Circuit Breaker: Resilience4j para tolerancia a fallos
- Actuator para health checks
- Clave compuesta: OrderItem identificado por orderId y productId

## Endpoints

Prefijo: `/shipping-service`

### OrderItem API

```
GET    /api/shippings                              - Listar todos los order items
GET    /api/shippings/{orderId}/{productId}        - Obtener order item por ID compuesto
GET    /api/shippings/find                         - Obtener order item por ID (POST con body)
POST   /api/shippings                              - Crear order item
PUT    /api/shippings                              - Actualizar order item
DELETE /api/shippings/{orderId}/{productId}        - Eliminar order item por ID compuesto
DELETE /api/shippings/delete                       - Eliminar order item (POST con body)
```

**Ejemplo de payload para crear order item:**

```json
{
  "orderId": 1,
  "productId": 1,
  "orderedQuantity": 2,
  "product": {
    "productId": 1
  },
  "order": {
    "orderId": 1
  }
}
```

**Nota sobre ID compuesto:**

El OrderItem tiene una clave primaria compuesta por `orderId` y `productId`. Para obtener o eliminar un order item específico, se deben proporcionar ambos valores en la URL:
- `GET /api/shippings/1/5` - Obtiene el order item con orderId=1 y productId=5
- `DELETE /api/shippings/1/5` - Elimina el order item con orderId=1 y productId=5

## Testing

### Unit Tests

El servicio incluye pruebas unitarias para validar la lógica de negocio de order items.

```bash
./mvnw test
```

## Ejecutar

```bash
# Opción 1: Directamente
./mvnw spring-boot:run

# Opción 2: Compilar y ejecutar
./mvnw clean package
java -jar target/shipping-service-v0.1.0.jar
```

Service corre en: `http://localhost:8600/shipping-service`

## Configuración

### Circuit Breaker (Resilience4j)

El servicio está configurado con circuit breaker para tolerancia a fallos:

- Failure rate threshold: 50%
- Minimum number of calls: 5
- Sliding window size: 10
- Wait duration in open state: 5s
- Sliding window type: COUNT_BASED

### Service Discovery

El servicio se registra automáticamente en Eureka Server con el nombre `SHIPPING-SERVICE`.

### Health Checks

El servicio expone endpoints de health check a través de Spring Boot Actuator:

```
GET /shipping-service/actuator/health
```

## Funcionalidades Implementadas

- Gestión completa de order items (CRUD)
- Clave primaria compuesta (orderId + productId)
- Validaciones de campos requeridos
- Manejo de excepciones personalizado
- Circuit breaker para resiliencia
- Integración con Service Discovery (Eureka)
- Comunicación con Order Service y Product Service para validación

## Comunicación con Otros Servicios

El Shipping Service puede comunicarse con otros microservicios a través del API Gateway:

- **Order Service**: Para validar que la orden existe
- **Product Service**: Para validar que el producto existe y obtener información del producto

Todas las comunicaciones se realizan a través del API Gateway y el Service Discovery (Eureka).

## Notas Importantes

### OrderItem (Items de Orden)

- Un OrderItem representa un producto específico dentro de una orden
- Cada OrderItem está asociado a una orden (orderId) y un producto (productId)
- La cantidad ordenada se almacena en `orderedQuantity`
- La clave primaria es compuesta: `(orderId, productId)`

### Relación con Órdenes

- Los order items están vinculados a órdenes existentes
- Al crear un order item, se valida que la orden exista
- Al eliminar una orden, los order items asociados también se eliminan (dependiendo de la configuración de cascada)

### Relación con Productos

- Los order items están vinculados a productos existentes
- Al crear un order item, se valida que el producto exista
- El servicio puede obtener información del producto desde Product Service
