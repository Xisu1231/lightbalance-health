package com.lightbalance.health.domain;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class DailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate recordDate;
    private int calories;
    private int calorieTarget;
    private int water;
    private int waterTarget;
    private int protein;
    private int proteinTarget;
    private int carbs;
    private int carbsTarget;
    private int fat;
    private int fatTarget;
    private double sleepHours;
    private int sleepScore;
    private int steps;
    private int stepTarget;
    private int workoutMinutes;
    private int workoutTarget;
    private int moodScore;
    private int stressScore;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getCalorieTarget() {
        return calorieTarget;
    }

    public void setCalorieTarget(int calorieTarget) {
        this.calorieTarget = calorieTarget;
    }

    public int getWater() {
        return water;
    }

    public void setWater(int water) {
        this.water = water;
    }

    public int getWaterTarget() {
        return waterTarget;
    }

    public void setWaterTarget(int waterTarget) {
        this.waterTarget = waterTarget;
    }

    public int getProtein() {
        return protein;
    }

    public void setProtein(int protein) {
        this.protein = protein;
    }

    public int getProteinTarget() {
        return proteinTarget;
    }

    public void setProteinTarget(int proteinTarget) {
        this.proteinTarget = proteinTarget;
    }

    public int getCarbs() {
        return carbs;
    }

    public void setCarbs(int carbs) {
        this.carbs = carbs;
    }

    public int getCarbsTarget() {
        return carbsTarget;
    }

    public void setCarbsTarget(int carbsTarget) {
        this.carbsTarget = carbsTarget;
    }

    public int getFat() {
        return fat;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public int getFatTarget() {
        return fatTarget;
    }

    public void setFatTarget(int fatTarget) {
        this.fatTarget = fatTarget;
    }

    public double getSleepHours() {
        return sleepHours;
    }

    public void setSleepHours(double sleepHours) {
        this.sleepHours = sleepHours;
    }

    public int getSleepScore() {
        return sleepScore;
    }

    public void setSleepScore(int sleepScore) {
        this.sleepScore = sleepScore;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getStepTarget() {
        return stepTarget;
    }

    public void setStepTarget(int stepTarget) {
        this.stepTarget = stepTarget;
    }

    public int getWorkoutMinutes() {
        return workoutMinutes;
    }

    public void setWorkoutMinutes(int workoutMinutes) {
        this.workoutMinutes = workoutMinutes;
    }

    public int getWorkoutTarget() {
        return workoutTarget;
    }

    public void setWorkoutTarget(int workoutTarget) {
        this.workoutTarget = workoutTarget;
    }

    public int getMoodScore() {
        return moodScore;
    }

    public void setMoodScore(int moodScore) {
        this.moodScore = moodScore;
    }

    public int getStressScore() {
        return stressScore;
    }

    public void setStressScore(int stressScore) {
        this.stressScore = stressScore;
    }
}
