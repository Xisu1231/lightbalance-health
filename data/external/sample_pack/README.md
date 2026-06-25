# External Sample Pack

Collected on June 24, 2026 for the body-portrait and behavior-analysis module.

Files in this folder:

- `pima_indians_diabetes.csv`
  Source: `https://raw.githubusercontent.com/jbrownlee/Datasets/master/pima-indians-diabetes.data.csv`
  Use: blood-sugar and diabetes-risk sample source.

- `diabetes_multivariate.csv`
  Source: `https://vincentarelbundock.github.io/Rdatasets/csv/MLDataR/diabetes_data.csv`
  Use: symptom-driven diabetes screening sample source.

- `intersalt_blood_pressure.csv`
  Source: `https://vincentarelbundock.github.io/Rdatasets/csv/DAAG/intersalt.csv`
  Use: blood-pressure and sodium-related sample source.

- `heart_disease_patients.csv`
  Source: `https://vincentarelbundock.github.io/Rdatasets/csv/CardioDataSets/heartdisease_tbl_df.csv`
  Use: cardiovascular indicators, blood pressure, blood sugar, exercise-related risk.

- `bodyfat_athletes.csv`
  Source: `https://vincentarelbundock.github.io/Rdatasets/csv/ipsRdbs/bodyfat.csv`
  Use: body-fat and physique stratification sample source.

- `sleep75_behavior.csv`
  Source: `https://vincentarelbundock.github.io/Rdatasets/csv/wooldridge/sleep75.csv`
  Use: sleep duration, work time, lifestyle behavior sample source.

- `rdatasets_index.csv`
  Source: `https://raw.githubusercontent.com/vincentarelbundock/Rdatasets/master/datasets.csv`
  Use: local index for finding more public datasets later.

Suggested mapping to your app:

- Body type / physique: `bodyfat_athletes.csv`
- Blood glucose / diabetes trend: `pima_indians_diabetes.csv`, `diabetes_multivariate.csv`
- Blood pressure analysis: `intersalt_blood_pressure.csv`, `heart_disease_patients.csv`
- Sleep / behavior relationship: `sleep75_behavior.csv`

Recommended next step:

- Build one cleaned integration table from these files into `data/external/body_portrait_sample_pool.csv`
- Normalize fields such as `sleep_hours`, `blood_glucose`, `systolic_bp`, `body_fat`, `stress_score`, `steps`
- Then bind that table into the “样本池与来源登记” page
