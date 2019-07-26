# Docker Course Assignment


## Installation

### Requirements:

* Docker Engine: 17.12.0+

### Procedures:

1. Unzip or clone the repository:

    ```
    git clone https://github.com/TracyKretschmer/tasks-api.git && cd vertx-tasks
    ```
    
2. Build the Server Container:

    ```
    docker-compose build
    ```
    
3. Build the Client Container:

    ```
    docker build -t tasks-client -f Dockerfile-client .
    ```
    
## Running

1. Start the Server:

    ```
    docker-compose up -d
    ```
    
2. Run the test client:

    ```
    docker run --name tasks-client --network=tasks-dmz-network --env API_HOST_PATH=load-balancer --env API_HOST_PORT=80 --rm  tasks-client
    ```
    
## Extras

### Scalability

The server is configured behind a nginx proxy so it can be scaled

```
docker-compose up --scale tasks-server=3
```
    
Reference: https://github.com/jwilder/nginx-proxy

### Playing around with the API:

The api is defined in the Open API specification format, the definition can be found in tasks-server/api/api.yaml

Example commands:

1. Create a new Task:

    ```
    curl -X POST \
      http://localhost:8080/tasks \
      -H 'Content-Type: application/json' \
      -d '{
      "title": "Create Container",
      "description": "Create a docker container 3",
      "startDate": 152755200000,
      "endDate": 152755200000,
      "priority": 2,
      "category": "docker",
      "status": "scheduled"
    }'
    ```
    
2. Get Task List:

    ```
    curl -X GET http://localhost:8080/tasks
    ```
      

3. Get Single Task:

    ```
    curl -X GET http://localhost:8080/tasks/{{id}}
    ```

4. Delete Task:

    ```
    curl -X DELETE http://localhost:8080/tasks/{{id}}
    ```