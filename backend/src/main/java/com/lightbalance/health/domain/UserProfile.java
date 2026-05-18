package com.lightbalance.health.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String handleName;
    private String goal;
    private double weight;
    private double targetWeight;
    private double bmi;
    private double bodyFat;
    private double visceralFat;
    private double muscleRate;
    private double basalMetabolism;
    private int healthScore;
    private String riskLevel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHandleName() {
        return handleName;
    }

    public void setHandleName(String handleName) {
        this.handleName = handleName;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getTargetWeight() {
        return targetWeight;
    }

    public void setTargetWeight(double targetWeight) {
        this.targetWeight = targetWeight;
    }

    public double getBmi() {
        return bmi;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public double getBodyFat() {
        return bodyFat;
    }

    public void setBodyFat(double bodyFat) {
        this.bodyFat = bodyFat;
    }

    public double getVisceralFat() {
        return visceralFat;
    }

    public void setVisceralFat(double visceralFat) {
        this.visceralFat = visceralFat;
    }

    public double getMuscleRate() {
        return muscleRate;
    }

    public void setMuscleRate(double muscleRate) {
        this.muscleRate = muscleRate;
    }

    public double getBasalMetabolism() {
        return basalMetabolism;
    }

    public void setBasalMetabolism(double basalMetabolism) {
        this.basalMetabolism = basalMetabolism;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}
