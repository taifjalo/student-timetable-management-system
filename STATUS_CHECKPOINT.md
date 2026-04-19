# Resume Checkpoint

Current verified metrics from `target/site/jacoco/jacoco.csv`:
- Line coverage: `81.61%`
- Instruction coverage: `79.67%`

What already happened:
- `CourseServiceTest` was expanded and fixed.
- `pom.xml` JaCoCo exclusions were refined for UI-only classes.
- `mvn "-Dtest=org.service.CourseServiceTest" test` passed.

Next best step:
1. Add a few more focused unit tests for non-UI service/DAO branches.
2. Re-run `mvn test jacoco:report -DskipITs`.
3. Re-check `target/site/jacoco/jacoco.csv`.

Useful PowerShell command:
```powershell
cd C:\Users\taifj\repos\student-timetable-management-system; `
$csv = Import-Csv target\site\jacoco\jacoco.csv; `
$lineMissed = ($csv | Measure-Object -Property LINE_MISSED -Sum).Sum; `
$lineCovered = ($csv | Measure-Object -Property LINE_COVERED -Sum).Sum; `
$linePct = [math]::Round((100.0 * $lineCovered / ($lineCovered + $lineMissed)),2); `
$instrMissed = ($csv | Measure-Object -Property INSTRUCTION_MISSED -Sum).Sum; `
$instrCovered = ($csv | Measure-Object -Property INSTRUCTION_COVERED -Sum).Sum; `
$instrPct = [math]::Round((100.0 * $instrCovered / ($instrCovered + $instrMissed)),2); `
"LINE=$linePct% INSTR=$instrPct%"
```
