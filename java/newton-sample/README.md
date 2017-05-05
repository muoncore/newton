**Features:**
* Event sourced Aggregate
* Unique domain name service
* View populated by listening to event stream 

**To test:**

```bash
curl -X POST \
  http://localhost:8080/api/todos \
  -H 'content-type: application/json' \
  -d '{"description":"Task1"}'
```
