import pandas as pd
import matplotlib.pyplot as plt

csv_file_path = input("Enter the CSV file path: ").strip()
df = pd.read_csv(csv_file_path)
df["MaxQDiff"] = pd.to_numeric(df["MaxQDiff"], errors="coerce")

probabilities = {1: 0.1, 2: 0.3, 3: 0.5, 4: 0.7, 5: 0.9}
run_numbers = sorted(df["RunNumber"].unique())

for run in run_numbers:
    run_data = df[df["RunNumber"] == run].set_index("EpisodeNumber")["MaxQDiff"]
    p_val = probabilities.get(run, run)
    
    moving_avg = run_data.rolling(window=10, min_periods=1).mean()
    plt.figure(figsize=(12,8))
    plt.plot(run_data.index, run_data.values, marker='o', linestyle='-', 
             label=f'p={p_val} Max QDiff')
    plt.plot(moving_avg.index, moving_avg.values, marker='o', linestyle='--', 
             label=f'p={p_val} Moving Avg (10 episode window)')
    plt.xlabel("Training Episode Number")
    plt.ylabel("Difference in Max Q-values for State 0 between episodes")
    plt.title(f"Change in Max Q-values for State 0 (p={p_val})")
    plt.legend()
    plt.grid(True)
    plt.show()
    
    moving_var = run_data.rolling(window=10, min_periods=1).var()
    plt.figure(figsize=(12,8))
    plt.plot(moving_var.index, moving_var.values, marker='o', linestyle='-', 
             label=f'p={p_val} Moving Variance (10 episode window)')
    plt.xlabel("Training Episode Number")
    plt.ylabel("Variance of change in Max Q-values for State 0 between episodes")
    plt.title(f"Rolling Variance of change in Max Q-values for State 0 (p={p_val})")
    plt.legend()
    plt.grid(True)
    plt.show()

avg_data = df.groupby("EpisodeNumber")["MaxQDiff"].mean()
avg_moving_avg = avg_data.rolling(window=10, min_periods=1).mean()

plt.figure(figsize=(12,8))
plt.plot(avg_data.index, avg_data.values, marker='o', linestyle='-', color='orange', label='Difference in Maximum Q-value for state 0')
plt.plot(avg_moving_avg.index, avg_moving_avg.values, marker='o', linestyle='--', color='lime', label='Moving Average of Difference in Maximum Q-value for state 0 (10 episode window)')
plt.xlabel("Training Episode Number")
plt.ylabel("Difference in Max Q-values for State 0 between episodes")
plt.title("Change in Max Q-values for State 0 between episodes in one full program run (averaged over 5 runs)")
plt.legend()
plt.grid(True)
plt.show()

avg_moving_var = avg_data.rolling(window=10, min_periods=1).var()
plt.figure(figsize=(12,8))
plt.plot(avg_moving_var.index, avg_moving_var.values, marker='o', linestyle='-', color='blue', label='Moving Variance of Difference in Maximum Q-value for state 0 (10 episode window)')
plt.xlabel("Training Episode Number")
plt.ylabel("Variance of change in Max Q-values for State 0 between episodes (10 episode window)")
plt.title("Rolling Variance of change in Max Q-values for State 0 in one full program run (10 episode window) (averaged over 5 runs)")
plt.legend()
plt.grid(True)
plt.show()