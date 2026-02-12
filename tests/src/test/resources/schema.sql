-- Student Timetable Database Schema (InnoDB required for transactions)

CREATE TABLE IF NOT EXISTS USER (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    surname       VARCHAR(100) NOT NULL,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password      VARCHAR(255) NOT NULL,
    role          VARCHAR(50) NOT NULL,
    puhelin       VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `GROUP` (
    group_code        VARCHAR(50) PRIMARY KEY,
    field_of_studies  VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS STUDENT_PROFILE (
    user_id           INT PRIMARY KEY,
    study_start_date  DATE,
    study_end_date    DATE,
    group_code        VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE CASCADE,
    FOREIGN KEY (group_code) REFERENCES `GROUP`(group_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS COURSE (
    course_id   INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    color_code  VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS LESSON (
    lesson_id   INT AUTO_INCREMENT PRIMARY KEY,
    start_at    DATETIME NOT NULL,
    end_at      DATETIME NOT NULL,
    class       VARCHAR(50) NOT NULL,
    course_id   INT NOT NULL,
    FOREIGN KEY (course_id) REFERENCES COURSE(course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ASSIGNED (
    lesson_id   INT NOT NULL,
    group_code  VARCHAR(50) NOT NULL,
    PRIMARY KEY (lesson_id, group_code),
    FOREIGN KEY (lesson_id) REFERENCES LESSON(lesson_id),
    FOREIGN KEY (group_code) REFERENCES `GROUP`(group_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS USER_LESSON (
    user_id     INT NOT NULL,
    lesson_id   INT NOT NULL,
    PRIMARY KEY (user_id, lesson_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id),
    FOREIGN KEY (lesson_id) REFERENCES LESSON(lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS MESSAGE (
    message_id        INT AUTO_INCREMENT PRIMARY KEY,
    sent_at           DATETIME NOT NULL,
    content           TEXT NOT NULL,
    sender_user_id    INT NOT NULL,
    recipient_user_id INT NOT NULL,
    FOREIGN KEY (sender_user_id) REFERENCES USER(user_id),
    FOREIGN KEY (recipient_user_id) REFERENCES USER(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
