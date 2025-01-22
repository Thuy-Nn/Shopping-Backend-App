# About the Project
This project was developed as part of the university module "Verteilte Anwendungen." It is a backend application designed to be used by a webshop as a shopping cart and for processing orders.

For more detailed information about the project's functionality and the tasks I completed, please refer to the `README.md` file located in the `src` directory.

## Stack
Spring Boot, Quarkus, JPA, Hibernate, Liquibase, Redis, RestAssured

## Set up local

- Build database Mysql and cache Redis
```
docker-compose up -d
```
- Run Application
```
mvn package
```