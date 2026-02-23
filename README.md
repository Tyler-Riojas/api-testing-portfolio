# API Testing Portfolio

A collection of API tests built using Postman and RestAssured.

## Tools
- Postman (manual + scripted API testing)
- RestAssured (Java-based automated API testing)
- Maven (dependency management)

## Structure
- `/postman` - Postman collections and environments
- `/rest-assured` - Java/RestAssured test project
- `/docs` - Notes and references

## Practice APIs
- Restful Booker (https://restful-booker.herokuapp.com)

## How to Run Postman Tests
1. Clone this repo
2. Open Postman
3. Click **Import** and select the collection from `postman/collections/`
4. Click **Import** again and select the environment from `postman/environments/`
5. Select the `Restful Booker` environment from the top right dropdown
6. Right click the collection and click **Run collection**
7. All 12 tests should pass
