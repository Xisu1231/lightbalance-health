CREATE DATABASE IF NOT EXISTS health_balance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE health_balance;

CREATE TABLE IF NOT EXISTS user_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50),
  handle_name VARCHAR(50),
  goal VARCHAR(255),
  weight DOUBLE,
  target_weight DOUBLE,
  bmi DOUBLE,
  body_fat DOUBLE,
  visceral_fat DOUBLE,
  muscle_rate DOUBLE,
  basal_metabolism DOUBLE,
  health_score INT,
  risk_level VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS daily_summary (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  record_date DATE,
  calories INT,
  calorie_target INT,
  water INT,
  water_target INT,
  protein INT,
  protein_target INT,
  carbs INT,
  carbs_target INT,
  fat INT,
  fat_target INT,
  sleep_hours DOUBLE,
  sleep_score INT,
  steps INT,
  step_target INT,
  workout_minutes INT,
  workout_target INT,
  mood_score INT,
  stress_score INT
);

CREATE TABLE IF NOT EXISTS trend_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  record_date DATE,
  weight DOUBLE,
  sleep_hours DOUBLE,
  steps INT,
  calories INT,
  stress_score INT
);

CREATE TABLE IF NOT EXISTS meal_entry (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100),
  meal_type VARCHAR(30),
  portion VARCHAR(30),
  calories INT,
  protein INT,
  carbs INT,
  fat INT,
  eaten BIT,
  recommended_time TIME
);

CREATE TABLE IF NOT EXISTS workout_plan (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100),
  category VARCHAR(50),
  duration INT,
  calories_burned INT,
  intensity VARCHAR(30),
  completed BIT
);
