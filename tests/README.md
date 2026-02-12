# Student Timetable - Azure MySQL Integration Tests

Plain JDBC integration tests for Azure Database for MySQL (Flexible Server). No Spring.

## Environment Variables

**Required:**
| Variable | Example |
|----------|---------|
| `DB_USER` | `sex@timetable-student` |
| `DB_PASSWORD` | `sexGroup6` |

**Optional:**
| Variable | Default |
|----------|---------|
| `DB_NAME` | `student_timetable` |

```powershell
$env:DB_USER = "sex@timetable-student"
$env:DB_PASSWORD = "sexGroup6"
```

## Run Tests

```bash
mvn test
```

## Project Structure

```
src/main/java/com/example/db/Db.java              # Connection helper
src/test/java/com/example/db/StudentTimetableAzureIT.java  # Tests
src/test/resources/schema.sql                      # DDL
```

## Test Pattern

All tests use transactional rollback:
1. `setAutoCommit(false)`
2. Insert/update/query
3. Assert changes visible in TX
4. `rollback()` in finally
5. New connection confirms no persistence

**Note:** Rollback only works for DML on InnoDB. DDL/TRUNCATE cause implicit commit.

## Azure Troubleshooting

- **Firewall:** Add your IP in Azure Portal
- **Username:** Must be `user@servername` format
- **Port 3306:** Must be open outbound
- **SSL:** Use `sslMode=REQUIRED` (minimum)
- **Production SSL:** Use `VERIFY_CA` or `VERIFY_IDENTITY` with truststore

## Tests Included

1. Smoke test (SELECT 1)
2. MESSAGE insert/rollback
3. LESSON FK violation
4. Multi-table (USER + STUDENT_PROFILE + MESSAGE) rollback
5. Schema verification

## Testcontainers Alternative

For local/CI isolation without Azure:

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <version>1.19.8</version>
    <scope>test</scope>
</dependency>
```
