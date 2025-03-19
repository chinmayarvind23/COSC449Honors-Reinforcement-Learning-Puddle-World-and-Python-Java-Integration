import numpy as np
import matplotlib.pyplot as plt

k = np.arange(1, 51)
p_values = [0.1, 0.3, 0.5]
plt.figure(figsize=(10, 6))

for p in p_values:
    pmf = (1 - p)**(k - 1) * p
    plt.plot(k, pmf, marker='o', linestyle='-', label=f'p = {p}')

plt.xlabel("Number of training steps in an episode before termination (k)")
plt.ylabel("Probability of training episode terminating at exactly k training steps")
plt.xticks(np.arange(0, 51, 10))
plt.legend()
plt.grid(axis='y', linestyle='--', alpha=0.7)
plt.show()