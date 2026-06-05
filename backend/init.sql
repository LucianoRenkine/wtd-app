-- USUARIOS
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  email VARCHAR UNIQUE NOT NULL,
  nombre VARCHAR NOT NULL,
  password_hash VARCHAR NOT NULL,
  role VARCHAR DEFAULT 'user',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CALENDARIO/TAREAS
CREATE TABLE tareas (
  id SERIAL PRIMARY KEY,
  user_id INT REFERENCES users(id) ON DELETE CASCADE,
  titulo VARCHAR NOT NULL,
  descripcion TEXT,
  fecha DATE NOT NULL,
  hora TIME,
  tipo VARCHAR CHECK (tipo IN ('evento', 'tarea', 'parcial', 'entrega')),
  completada BOOLEAN DEFAULT FALSE,
  materia VARCHAR,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- PELÍCULAS/SERIES
CREATE TABLE watchlist (
  id SERIAL PRIMARY KEY,
  user_id INT REFERENCES users(id) ON DELETE CASCADE,
  titulo VARCHAR NOT NULL,
  tipo VARCHAR CHECK (tipo IN ('película', 'serie')),
  estado VARCHAR DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'viendo', 'completado')),
  fecha_agregada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  temporada_actual INT,
  episodio_actual INT
);

-- CLASES DICTADAS
CREATE TABLE clases (
  id SERIAL PRIMARY KEY,
  user_id INT REFERENCES users(id) ON DELETE CASCADE,
  estudiante VARCHAR NOT NULL,
  monto DECIMAL(10, 2) NOT NULL,
  fecha DATE NOT NULL,
  pagado BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- MATERIAS
CREATE TABLE materias (
  id SERIAL PRIMARY KEY,
  user_id INT REFERENCES users(id) ON DELETE CASCADE,
  nombre VARCHAR NOT NULL,
  ano INT NOT NULL,
  semestre INT,
  carrera VARCHAR CHECK (carrera IN ('Informática', 'Psicología'))
);

-- NOTAS (parciales y finales)
CREATE TABLE notas (
  id SERIAL PRIMARY KEY,
  materia_id INT REFERENCES materias(id) ON DELETE CASCADE,
  tipo VARCHAR CHECK (tipo IN ('parcial', 'final')),
  nota DECIMAL(4, 2),
  fecha DATE,
  comentario TEXT
);

