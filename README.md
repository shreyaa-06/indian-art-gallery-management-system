# ShilpSangraha

A portfolio-quality **Indian Heritage Art Gallery Management System** built with vanilla HTML/CSS/JavaScript on the frontend and Java + JDBC + MySQL on the backend.

## Features

- **Authentication** — Login, logout, session-based access control
- **Dashboard** — Artwork/artist/exhibition counts and recent activity
- **Artworks** — CRUD, search, category filter, card grid, detail page
- **Artists** — CRUD and search
- **Exhibitions** — Create, schedule, assign artworks, view history
- **Customers** — Store visitor information and search
- **Reports** — Collection analytics and status breakdowns

## Tech Stack

| Layer | Technologies |
|-------|-------------|
| Frontend | HTML5, CSS3, JavaScript (ES6) |
| Backend | Java 17, JDBC, embedded HTTP server |
| Database | MySQL 8 |
| Build | Maven |

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+

## Setup

### 1. Create the database

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p art_gallery_db < database/seed.sql
```

### 2. Configure database connection

Edit `src/main/resources/application.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/art_gallery_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=your_password
server.port=8080
```

### 3. Build and run

```bash
mvn clean package
java -jar target/art-gallery-1.0.0.jar
```

Or run directly:

```bash
mvn compile exec:java -Dexec.mainClass="com.artgallery.Main"
```

### 4. Open the application

Visit [http://localhost:8080](http://localhost:8080)

**Default credentials:** `admin` / `admin123`

## Project Structure

```
art_gallery/
├── database/           # SQL schema and seed data
├── src/main/java/      # Java backend (MVC)
│   └── com/artgallery/
│       ├── config/     # Database & app configuration
│       ├── model/      # Entity POJOs
│       ├── dao/        # JDBC data access
│       ├── service/    # Business logic
│       ├── controller/ # HTTP handlers
│       └── util/       # Helpers
├── src/main/resources/ # application.properties
└── web/                # Frontend
    ├── pages/          # HTML pages
    ├── css/            # Stylesheets
    ├── js/             # JavaScript modules
    └── assets/         # Images & fonts
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/logout` | Logout |
| GET | `/api/auth/me` | Current user |
| GET | `/api/dashboard` | Dashboard stats |
| GET/POST | `/api/artworks` | List / create artworks |
| GET/PUT/DELETE | `/api/artworks/:id` | Artwork CRUD |
| GET/POST | `/api/artists` | List / create artists |
| GET/PUT/DELETE | `/api/artists/:id` | Artist CRUD |
| GET/POST | `/api/exhibitions` | List / create exhibitions |
| PUT | `/api/exhibitions/:id/artworks` | Assign artworks |
| GET/POST | `/api/customers` | List / create customers |
| GET | `/api/reports` | Report summary |

## License

MIT
