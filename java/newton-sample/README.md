**Features:**
* Event sourced Aggregate
* Unique domain name service
* View populated by listening to event stream 

**Tests:**

_Create new task:_

```bash
curl -X POST \
  http://localhost:9090/api/todos \
  -H 'content-type: application/json' \
  -d '{"description":"Task1"}'
```

_Change task description:_

```bash
curl -X PUT \
  http://localhost:9090/api/todos/{id} \
  -H 'content-type: application/json' \
  -d '{"description":"Task1a"}'
```
