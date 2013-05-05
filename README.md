epcr-portal-api
=====================================

This RESTful API provides all the necessary endpoint for the web portal UI to work with the database and other serverside services.

Endpoints served are one of two types: Resource (entity) or RPC (action).  Typically, and action is associated with an entity and as such are usually at the end of a resource url.

Resource based Endpoints:

GET /users                          - a list of users in the system
GET /users/:username                - a user with the given username
GET /departments                    - a list of all departments configured in the system
GET /departments/:departmentCode    - a department with the given departmentCode
GET /incidents                      - a list of the incidents in the system
GET /incidents/:departmentCode      - a list of the incidents for a given department

RPC based Endpoints
POST /user/:username/login

Payload:
    
```javascript
{ "password" : "<somepassword>" }
```
Response:
    
```javascript
{ "results" : "success|fail", "token" : "<sometoken>", "ttl" : 320 }
```

Setup:
 * PlayFramework v 2.1.1 (required - http://playframework.org )
 * MongoDB 2.4.1 (Required - either Local or MongoHQ)
 * IntelliJ IDEA 12.1 (with Scala, SBT, PlayFramework and Grep Console plugins) - (Optional)
 * Scala 2.10.0
 * JDK 1.7.0_17

If working on a Mac, Homebrew is recommended (install Play, Mongo and Scala from there)
