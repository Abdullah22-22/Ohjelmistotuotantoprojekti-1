# Temperature Converter

Individual in-class assignment, Metropolia UAS.

A small JavaFX desktop app that converts temperatures and saves every conversion into a MariaDB database. The saved conversions are shown in a table inside the app, and you can delete them.

## 1. Assignment Description

The task was to build a desktop application with a GUI that does temperature conversions and stores the results in a database, then test it with JUnit and set up a build pipeline for it.

What the app has to do:

- Convert between Fahrenheit, Celsius and Kelvin (4 directions)
- Warn the user when the result is an extreme temperature
- Save every conversion to the database
- Show all saved conversions in a table, with refresh and delete
- Handle bad input without crashing

What I had to deliver:

- The working app, packaged as a runnable JAR
- JUnit tests for the logic and the database code
- A code coverage report
- A Dockerfile so the app runs in a container
- A Jenkins pipeline that builds, tests and pushes the Docker image
- This README

## 2. Technologies & Tools Used

- **Java 17** and **Maven** for the project itself
- **JavaFX 17.0.11** for the user interface
- **MariaDB** as the database, with the MariaDB JDBC driver 3.3.3
- **JUnit 5** (Jupiter 5.10.2) for the tests
- **H2 2.2.224** as an in-memory database for the tests only
- **JaCoCo 0.8.11** for code coverage
- **Maven Shade Plugin** to build a JAR with all dependencies inside
- **Docker** to containerise the app
- **Jenkins** for the CI pipeline, and **Docker Hub** to store the image
- Git and GitHub for version control

JavaFX is added twice in the pom.xml, once with the `win` classifier and once with `linux`. I did this because I develop on Windows but the Docker image runs on Linux, and this way the same JAR works in both.

## 3. Design Approach & Implementation Method

I split the code into four packages so each part has one job:

```
fi.metropolia.tempconverter
├── Launcher.java          starts the app
├── MainApp.java           the JavaFX window
├── model/
│   ├── TempCalculator     the conversion formulas
│   ├── TempRecord         one saved conversion
│   └── TemperatureUnit    one unit (Celsius, Fahrenheit, Kelvin)
├── dao/
│   ├── TempRecordDAO      database operations for records
│   └── TemperatureUnitDAO database operations for units
└── db/
    └── DBConnection       opens and closes the connection
```

### Why there is a Launcher class

A JavaFX `Application` class cannot be the main class of a shaded JAR. If you try, the JVM says "JavaFX runtime components are missing" and refuses to start. The usual fix is a tiny normal class that calls `MainApp.main()`, so that is what `Launcher` is. The shade plugin points at `Launcher` in the pom.xml.

### GUI

I built the interface in code instead of using FXML, since the window is small and keeping it in one method was easier to follow.

The window has a text field for the value, a dropdown with the four conversion directions, and three buttons: Convert & Save, Refresh, and Delete Selected. Below that is a `TableView` showing the saved records (ID, input, result, unit ID, time). At the bottom there is a status label that turns green on success and red on errors. All messages go through one `showStatus()` method so the behaviour is the same everywhere.

Before converting, the app checks that the field is not empty and that the text is actually a number. Both cases show a message instead of throwing an exception.

### Conversion logic

`TempCalculator` has no database and no JavaFX in it, only the formulas. That made it very easy to test. It also has `isExtremeTemperature()`, which returns true when the value is below -40 °C or above 50 °C. The two boundary values themselves are not counted as extreme.

Keeping this class separate from `MainApp` was the main design decision. If the formulas were written inside the button handlers I would not be able to test them at all.

### Database

Two tables:

```sql
temperature_units
  unit_id    INT AUTO_INCREMENT PRIMARY KEY
  unit_name  VARCHAR(20) NOT NULL
  symbol     VARCHAR(5)  NOT NULL

temp_records
  record_id        INT AUTO_INCREMENT PRIMARY KEY
  input_value      DOUBLE NOT NULL
  converted_value  DOUBLE NOT NULL
  unit_id          INT, foreign key to temperature_units
  created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

The timestamp is filled by the database, not by Java, so it does not depend on the computer's clock.

`DBConnection` keeps one static connection and reopens it if it is null or closed. It reads the database settings from the environment variables `DB_URL`, `DB_USER` and `DB_PASS`, and falls back to `localhost:3307` with root/root if they are not set. This is what lets the same JAR run on my machine and inside Docker without changing any code.

It also has a `setCredentials()` method. The tests use it to point the app at an H2 database instead of MariaDB.

In the DAO classes I used `PreparedStatement` everywhere so the queries are safe from SQL injection, and try-with-resources so nothing stays open. Inserts use `RETURN_GENERATED_KEYS` so the new ID comes back right away. In `TempRecordDAO` I put the ResultSet mapping into one private `mapRow()` method so the column names are written only once.

### Docker

The image is based on `eclipse-temurin:17-jdk`. JavaFX needs native graphics libraries, so the Dockerfile installs GTK, OpenGL and a few X11 libraries before copying the JAR. `DISPLAY` points to `host.docker.internal:0.0` so the window appears on the host's X server, and the database variables point back to the host as well.

### Jenkins pipeline

Seven stages: Build, Test, Code Coverage, Publish Test Results, Publish Coverage Report, Build Docker Image, Push to Docker Hub.

The Build stage runs with `-DskipTests` so I can tell a compile error apart from a failing test. Test results come from Surefire and coverage from JaCoCo, both published so Jenkins shows the trend between builds. The image gets two tags, the build number and `latest`. The Docker Hub password comes from Jenkins credentials and is never written in the repository.

## 4. Testing & Quality Assurance Steps

### How I tested

I wrote 37 tests in 6 classes. The model classes are tested on their own, and the DAO classes are tested against a real database.

For the DAO tests I had a choice between mocking JDBC and using a real database. I chose H2 running in memory with MySQL compatibility mode. With mocks I would only prove that my code calls JDBC, not that my SQL is correct. With H2 the real queries actually run, so mistakes in the SQL show up. It is still fast and it needs no database server, so it also works on the Jenkins agent.

Each test class uses its own in-memory database name and recreates the tables in `@BeforeEach`, so the tests do not affect each other and can run in any order.

### Test results

**TempCalculatorTest (6 tests)**

| Test | What it checks | Result |
|---|---|---|
| fahrenheitToCelsius | 32, 212, -40 and 98.6 °F | Pass |
| celsiusToFahrenheit | 0, 100 and -40 °C | Pass |
| kelvinToCelsius | 300 K, 273.15 K and 0 K | Pass |
| celsiusToKelvin | 0 and 100 °C | Pass |
| roundTripConversion | 25 °C to F and back to C | Pass |
| isExtremeTemperature | -41, 51, 1000 against -40, 50, 25, 0 | Pass |

I used -40 on purpose because it is the one point where Celsius and Fahrenheit are equal. If the formula was reversed, that test would still pass but the others would fail, so together they catch it. The extreme test checks both sides of each limit so the comparison stays inclusive.

**TemperatureUnitTest (4 tests)**

Empty constructor gives id 0 and null fields, the full constructor sets all three values, getters and setters work, and `toString()` returns `Fahrenheit (F)`. All pass.

**TempRecordTest (4 tests)**

Empty constructor defaults, full constructor, all getters and setters including the `LocalDateTime`, and negative values being allowed. All pass.

**DBConnectionTest (6 tests)**

| Test | What it checks | Result |
|---|---|---|
| getConnectionReturnsOpenConnection | Connection is not null and is open | Pass |
| getConnectionReusesSameConnection | Two calls return the same object | Pass |
| testConnectionReturnsTrueWhenOpen | Health check returns true | Pass |
| closeConnectionClosesIt | Connection really closes | Pass |
| connectionIsRecreatedAfterClose | A new one opens after closing | Pass |
| setCredentialsResetsConnection | Switching URL gives a new connection | Pass |

The reuse test proves the connection is cached, and the recreate test proves the caching does not leave the app permanently broken after a close.

**TemperatureUnitDAOTest (8 tests)**

The table is seeded with Celsius, Fahrenheit and Kelvin.

| Test | What it checks | Result |
|---|---|---|
| findAllReturnsThreeUnits | Reads the 3 seeded rows | Pass |
| findByIdReturnsCorrectUnit | ID 1 gives Celsius / C | Pass |
| findByIdReturnsNullForUnknownId | ID 999 gives null | Pass |
| findBySymbolReturnsCorrectUnit | Symbol K gives Kelvin | Pass |
| findBySymbolReturnsNullForUnknownSymbol | Symbol X gives null | Pass |
| insertAddsNewUnit | New unit gets an ID and can be read back | Pass |
| deleteRemovesUnit | Deleted unit is gone | Pass |
| deleteReturnsFalseForUnknownId | Deleting ID 999 returns false | Pass |

**TempRecordDAOTest (9 tests)**

Both tables are created with the foreign key, and `temp_records` starts empty.

| Test | What it checks | Result |
|---|---|---|
| tableIsEmptyAtStart | count is 0 and findAll is empty | Pass |
| insertReturnsGeneratedId | Insert returns a positive ID | Pass |
| insertStoresCorrectValues | All fields come back correctly, timestamp is set | Pass |
| findAllReturnsAllRecords | Three inserts give three rows | Pass |
| findByIdReturnsNullForUnknownId | ID 999 gives null | Pass |
| deleteRemovesRecord | Record is gone and count drops to 0 | Pass |
| deleteReturnsFalseForUnknownId | Deleting a missing ID returns false | Pass |
| deleteAllClearsTable | Returns 2 and empties the table | Pass |
| negativeTemperaturesAreStored | -40 keeps its sign | Pass |

`insertStoresCorrectValues` is the most useful one here, because it is the only test that proves the timestamp created by the database actually arrives back in Java as a real `LocalDateTime`.

**Total: 37 tests, 37 passed, 0 failed.**

All comparisons between doubles use a delta (0.01 for conversions, 0.001 for stored values, 0.0001 for the round trip) instead of exact equality.

### Coverage

JaCoCo runs during the test phase, so the report is generated on every build at `target/site/jacoco/index.html`.

The `model` and `dao` packages have the highest coverage. `MainApp` is not unit tested because JavaFX event handlers need a running toolkit to test properly. That is the reason I moved the formulas out into `TempCalculator`, so the untested part is only layout and button wiring, which I checked by hand.

### Manual testing

I ran these with MariaDB running on port 3307:

1. Started the app and the status bar showed the number of units from the database, so the connection works.
2. Entered 212 with Fahrenheit to Celsius. Got 100.00, green status, and a new row in the table.
3. Entered 1000 with Fahrenheit to Celsius and the result was marked (EXTREME!).
4. Entered 122 °F, which is exactly 50 °C, and it was not marked as extreme.
5. Clicked Convert with an empty field and got "Please enter a value" in red. Nothing was saved.
6. Entered "abc" and got "Invalid number" in red. No crash, nothing saved.
7. Entered -40 in both directions and the values saved correctly.
8. Added a row with SQL directly, then clicked Refresh and it appeared.
9. Clicked Delete Selected without selecting anything and got "Select a row first".
10. Selected a row and deleted it, then confirmed with a SELECT that it was gone from the database.
11. Stopped MariaDB and started the app again. It opened and showed the connection error in red instead of crashing.
12. Ran the packaged JAR with `java -jar` and it started without any module path arguments.
13. Built the Docker image and ran it with an X server on Windows. The window opened and connected to the database on the host.
14. Ran the Jenkins job and all seven stages passed. Test results and coverage appeared in Jenkins and both image tags showed up on Docker Hub.

## 5. How to Run

### What you need

- JDK 17
- Maven 3.6 or newer
- MariaDB or MySQL running on port 3307
- Docker and an X server, only if you want to run the container

The tests do not need MariaDB. They use H2 in memory.

### Database setup

```sql
CREATE DATABASE tempdb;
USE tempdb;
SOURCE schema.sql;
```

Then add the units:

```sql
INSERT INTO temperature_units (unit_name, symbol) VALUES
  ('Celsius','C'), ('Fahrenheit','F'), ('Kelvin','K');
```

Insert them in this order. The app saves records with unit IDs 1, 2 and 3, so they need to match.

### Build

```bash
git clone <repository-url>
cd <repository-directory>
mvn clean package
```

This compiles the code, runs all 37 tests, creates the coverage report and builds the JAR at `target/week3-junit-1.0-SNAPSHOT.jar`.

### Run

From Maven:

```bash
mvn javafx:run
```

Or from the JAR:

```bash
java -jar target/week3-junit-1.0-SNAPSHOT.jar
```

Or with Docker:

```bash
mvn clean package
docker build -t temperature-converter .

docker run --rm \
  -e DISPLAY=host.docker.internal:0.0 \
  -e DB_URL=jdbc:mariadb://host.docker.internal:3307/tempdb \
  -e DB_USER=root \
  -e DB_PASS=root \
  temperature-converter
```

Start the X server first and allow connections from the container. The image is also on Docker Hub:

```bash
docker pull abdullah22hel/temperature-converter:latest
```

### Settings

| Variable | Default |
|---|---|
| DB_URL | jdbc:mariadb://localhost:3307/tempdb |
| DB_USER | root |
| DB_PASS | root |

You can change any of these without rebuilding. For example use port 3306 if your server runs on the default port.

### Running the tests alone

```bash
mvn test
mvn test jacoco:report
```

Results are in `target/surefire-reports/` and the coverage report is at `target/site/jacoco/index.html`.

### Using the app

1. Type a number in the Value field.
2. Pick a conversion from the dropdown.
3. Click Convert & Save. The result appears, and it says (EXTREME!) if it is below -40 °C or above 50 °C. The conversion is saved to the database.
4. Click Refresh to reload the table, or select a row and click Delete Selected to remove it.
5. The status bar at the bottom shows what happened after every action.