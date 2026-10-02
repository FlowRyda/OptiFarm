# Backend Template for SE PR Group Phase

## How to run it

To start the backend, you need a Postgres instance. Easiest is to run via the postgres-docker-compose.yml

### Start the backend
`mvn spring-boot:run`

### Start the backed with test data
If the database is not clean, the test data won't be inserted

`mvn spring-boot:run -Dspring-boot.run.profiles=generateData`

For dev data, you can use the following command:

`mvn spring-boot:run -Dspring-boot.run.profiles=dev`
