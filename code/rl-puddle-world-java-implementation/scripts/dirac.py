import numpy as np
import matplotlib.pyplot as plt

k0 = 30
max_k = 50

k = np.arange(1, max_k+1)

pmf = np.zeros_like(k, dtype=float)
pmf[k == k0] = 1.0

plt.figure(figsize=(10, 6))
plt.bar(k, pmf, width=0.6, color='skyblue', edgecolor='black')

plt.xlabel("Number of training steps in an episode before termination (k)")
plt.ylabel("Probability of termination at exactly k steps")
plt.title(f"Degenerate Distribution PMF (maximum number of steps/k₀ = {k0})")
plt.xticks(np.arange(0, max_k+1, 10))
plt.xlim(0, max_k+10)
plt.text(max_k, 0.05, r'$\ldots$', fontsize=20, verticalalignment='center')
plt.grid(axis='y', linestyle='--', alpha=0.7)
plt.show()