# Tracksy Backend

Backend de Tracksy construido con Django, Django REST Framework y PostgreSQL.

## Requisitos previos

Instalar manualmente antes de levantar el proyecto:

- Python 3.11 recomendado.
- PostgreSQL corriendo localmente o accesible por red.
- `pip`.
- Conda, si se usa el flujo con Conda.
- Credenciales de Firebase Admin SDK, necesarias para autenticar requests con Firebase.

El backend se ejecuta desde la carpeta `tracksy_backend/`.

```bash
cd tracksy_backend
```

## Opcion A: entorno con Conda

Crear y activar el entorno:

```bash
conda create -n tracksy-backend python=3.11
conda activate tracksy-backend
```

Instalar dependencias:

```bash
pip install -r requirements.txt
pip install django-debug-toolbar
```

`django-debug-toolbar` se instala aparte porque el settings de desarrollo lo importa, pero actualmente no esta listado en `requirements.txt`.

## Opcion B: entorno con venv

Crear y activar el entorno:

```bash
python3 -m venv .venv
source .venv/bin/activate
```

En Windows:

```bash
python -m venv .venv
.\.venv\Scripts\activate
```

Instalar dependencias:

```bash
pip install --upgrade pip
pip install -r requirements.txt
pip install django-debug-toolbar
```

## Variables de entorno

Crear el archivo `.env` a partir del ejemplo:

```bash
cp .env.example .env
```

Editar `.env` manualmente con los valores locales:

```env
SECRET_KEY=una-clave-local
DEBUG=True
ALLOWED_HOSTS=localhost,127.0.0.1

DB_NAME=tracksy_db
DB_USER=tracksy_user
DB_PASSWORD=tracksy_password
DB_HOST=localhost
DB_PORT=5432

JWT_ACCESS_TOKEN_LIFETIME_MINUTES=60
JWT_REFRESH_TOKEN_LIFETIME_DAYS=7

FIREBASE_PROJECT_ID=tracksy-bc9d4
FIREBASE_CREDENTIALS_PATH=/ruta/absoluta/firebase-service-account.json

MEDIA_ROOT=media/
MEDIA_URL=/media/

CORS_ALLOWED_ORIGINS=http://localhost:3000,http://10.0.2.2:8000
```

Notas:

- `FIREBASE_CREDENTIALS_PATH` debe apuntar a un archivo JSON real del Firebase Admin SDK.
- `DB_*` debe coincidir con la base de datos y usuario creados en PostgreSQL.
- `manage.py` usa `config.settings.development` por defecto.

## Base de datos PostgreSQL

Crear usuario y base de datos manualmente. Una forma posible:

```bash
sudo -u postgres psql
```

Dentro de `psql`:

```sql
CREATE USER tracksy_user WITH PASSWORD 'tracksy_password';
CREATE DATABASE tracksy_db OWNER tracksy_user;
GRANT ALL PRIVILEGES ON DATABASE tracksy_db TO tracksy_user;
\q
```

Si ya existe un usuario PostgreSQL con permisos, tambien se puede crear solo la base:

```bash
createdb tracksy_db
```

Luego ajustar `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_HOST` y `DB_PORT` en `.env`.

## Migraciones

Aplicar migraciones:

```bash
python manage.py migrate
```

Crear un usuario administrador para entrar al panel admin:

```bash
python manage.py createsuperuser
```

## Levantar el backend

Ejecutar el servidor de desarrollo:

```bash
python manage.py runserver
```

Por defecto queda disponible en:

- API: `http://localhost:8000/api/v1/`
- Swagger: `http://localhost:8000/api/docs/`
- ReDoc: `http://localhost:8000/api/redoc/`
- Admin: `http://localhost:8000/tracksy-admin/`

Para escuchar en todas las interfaces, por ejemplo para probar desde un emulador o dispositivo en la red:

```bash
python manage.py runserver 0.0.0.0:8000
```

## Comandos utiles

Verificar configuracion y problemas comunes:

```bash
python manage.py check
```

Generar migraciones despues de cambiar modelos:

```bash
python manage.py makemigrations
python manage.py migrate
```

Entrar a la shell de Django:

```bash
python manage.py shell
```

Ejecutar tests si se agregan tests al proyecto:

```bash
pytest
```

## Checklist manual de arranque

1. Entrar a `tracksy_backend/`.
2. Crear y activar el entorno con Conda o venv.
3. Instalar `requirements.txt`.
4. Instalar `django-debug-toolbar`.
5. Crear `.env` desde `.env.example`.
6. Completar `.env` con PostgreSQL y Firebase.
7. Crear la base de datos PostgreSQL.
8. Ejecutar `python manage.py migrate`.
9. Ejecutar `python manage.py createsuperuser`.
10. Ejecutar `python manage.py runserver`.
