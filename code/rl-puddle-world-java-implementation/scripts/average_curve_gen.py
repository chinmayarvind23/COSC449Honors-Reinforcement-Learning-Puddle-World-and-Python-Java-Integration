import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv("/Users/chinmayarvind/Documents/UBC/UBC Coursework/COSC 449/Reinforcement-Learning-Puddle-World-and-Python-Java-Integration/code/rl-puddle-world-java-implementation/data/agent_effectiveness_data.csv")
avg_q = df.groupby("EpisodeNumber")["MaxQValueForState0"].mean()

plt.figure(figsize=(8, 6))
plt.plot(avg_q.index, avg_q.values, marker='o', label='Average over 5 runs')
plt.xlabel("Episode Number")
plt.ylabel("Average Max Q-Value for State 0")
plt.title("Max Q-value for state 0 as a function of the number of episodes (averaged over 5 runs)")
plt.legend()
plt.grid(True)
plt.show()