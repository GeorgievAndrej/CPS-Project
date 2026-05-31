users
admin pw: password
profesor pw: password
student001 pw: password


CREATE DATABASE IF NOT EXISTS cps_db CHARACTER SET utf8mb4;
USE cps_db;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role ENUM('admin','teacher','student') NOT NULL DEFAULT 'student',
    student_id VARCHAR(50) UNIQUE,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE courses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(500),
    teacher_id INT NOT NULL,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

CREATE TABLE attendance_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_external_id VARCHAR(50) NOT NULL,
    student_name VARCHAR(255) NOT NULL,
    course_id INT NOT NULL,
    teacher_id INT NOT NULL,
    tapped_at DATETIME NOT NULL,
    synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_id VARCHAR(100),
    is_duplicate TINYINT(1) DEFAULT 0,
    INDEX idx_course_tapped (course_id, tapped_at),
    INDEX idx_student_course (student_external_id, course_id),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

INSERT INTO users (username, password, full_name, email, role) VALUES
('admin', '$2y$10$BoZpLCkQTHFyz.gAECEPAujjL2Vza56tTq39H3FjftTcazdl6gNmS', 'System Admin', 'admin@cps.com', 'admin'),
('profesor', '$2y$10$BoZpLCkQTHFyz.gAECEPAujjL2Vza56tTq39H3FjftTcazdl6gNmS', 'Profesor', 'profesor@cps.com', 'teacher');

INSERT INTO users (username, password, full_name, email, role, student_id) VALUES
('student001', '$2y$10$BoZpLCkQTHFyz.gAECEPAujjL2Vza56tTq39H3FjftTcazdl6gNmS', 'Student', 'student@cps.com', 'student', 'S001');

INSERT INTO courses (name, code, teacher_id) VALUES
('Mathematics 101', 'MATH101', 2),
('Physics 201', 'PHYS201', 2);