# Finsible Documentation

Welcome to the Finsible documentation. This folder contains comprehensive documentation for the Finsible Personal Finance Management API.

## 📚 Documentation Index

| Document                               | Description                                                     |
|----------------------------------------|-----------------------------------------------------------------|
| [Quick Start Guide](QUICKSTART.md)     | Get up and running in 5 minutes                                 |
| [API Documentation](API.md)            | Complete API reference with endpoints, parameters, and examples |
| [OpenAPI Specification](openapi.yaml)  | OpenAPI 3.0 spec for use with Swagger UI, Postman, etc.         |
| [System Architecture](ARCHITECTURE.md) | Architecture diagrams, component design, and data flows         |
| [Database Schema](DATABASE.md)         | Database tables, relationships, and ERD                         |
| [Security](SECURITY.md)                | Authentication, authorization, and security practices           |
| [Troubleshooting](TROUBLESHOOTING.md)  | Common issues and solutions                                     |

## 🚀 Getting Started

1. **New to Finsible?** Start with the [Quick Start Guide](QUICKSTART.md)
2. **Integrating with Finsible?** Check the [API Documentation](API.md)
3. **Understanding the system?** Read the [Architecture](ARCHITECTURE.md) doc
4. **Having issues?** See [Troubleshooting](TROUBLESHOOTING.md)

## 📖 Using the OpenAPI Specification

The `openapi.yaml` file can be used with various tools:

### Swagger UI

```bash
# Using Docker
docker run -p 8080:8080 -e SWAGGER_JSON=/docs/openapi.yaml -v $(pwd):/docs swaggerapi/swagger-ui
```

### Postman

1. Open Postman
2. Click "Import"
3. Select the `openapi.yaml` file
4. A new collection will be created with all endpoints

### Code Generation

```bash
# Generate client SDK (example with openapi-generator)
npx @openapitools/openapi-generator-cli generate \
  -i openapi.yaml \
  -g typescript-fetch \
  -o ./generated-client
```

## 📝 Document Maintenance

When updating documentation:

1. **API Changes**: Update both `API.md` and `openapi.yaml`
2. **Database Changes**: Update `DATABASE.md` and ensure Flyway migrations are documented
3. **Architecture Changes**: Update `ARCHITECTURE.md` with new diagrams
4. **Security Changes**: Update `SECURITY.md` with new configurations

## 🔗 External Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [JWT.io](https://jwt.io/) - JWT Debugger
- [Google OAuth Documentation](https://developers.google.com/identity)

---

*Documentation last updated: January 2025*
