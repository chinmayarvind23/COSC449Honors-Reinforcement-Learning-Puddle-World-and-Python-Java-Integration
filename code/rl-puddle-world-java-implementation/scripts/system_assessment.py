#!/usr/bin/env python3
import re
import csv
import sys
import os

def parse_client_logs(client_log_path):
    """
      === End of Episode Summary ===
      Steps Taken: 20/30
      Discounted Episode Reward: -5.627220814338541
      ===============================================
    """
    episodes = {}
    with open(client_log_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    episode = 0
    i = 0
    while i < len(lines):
        line = lines[i].strip()
        if line == "=== End of Episode Summary ===":
            episode += 1
            if i + 2 < len(lines):
                steps_line = lines[i + 1].strip()  # "Steps Taken: 20/30"
                reward_line = lines[i + 2].strip() # "Discounted Episode Reward: -5.627220814338541"
                m_steps = re.search(r'^Steps Taken:\s+(\d+)/', steps_line)
                if m_steps:
                    try:
                        client_steps = int(m_steps.group(1))
                    except ValueError:
                        client_steps = None
                else:
                    client_steps = None

                # Extract the reward after the colon
                m_reward = re.search(r'^Discounted Episode Reward:\s+([-+]?\d*\.?\d+(?:[eE][-+]?\d+)?)', reward_line)
                if m_reward:
                    try:
                        client_reward = float(m_reward.group(1))
                    except ValueError:
                        client_reward = None
                else:
                    client_reward = None

                episodes[episode] = (client_steps, client_reward)
                i += 2  # Skip the two lines we just processed
        i += 1

    return episodes

def parse_server_logs(server_log_path):
    """
      End of episode summary:
       - Total Episodes: 1
       - Successful Episodes: 0
       - Steps Taken: 26
       - Discounted Episode Reward: -8.595328550337097
    """
    episodes = {}
    error_count = 0

    with open(server_log_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    # Count "error" lines (case-insensitive)
    for line in lines:
        if re.search(r'error', line, re.IGNORECASE):
            error_count += 1

    episode = 0
    i = 0
    while i < len(lines):
        line = lines[i].strip()
        if "End of episode summary:" in line:
            episode += 1
            if i + 4 < len(lines):
                steps_line = lines[i + 3].strip()    # " - Steps Taken: 26"
                reward_line = lines[i + 4].strip()   # "- Discounted Episode Reward: -8.595328550337097"
                m_steps = re.search(r'Steps Taken:\s*(\d+)', steps_line)
                if m_steps:
                    try:
                        server_steps = int(m_steps.group(1))
                    except ValueError:
                        server_steps = None
                else:
                    server_steps = None

                m_reward = re.search(r'Discounted Episode Reward:\s*([-+]?\d*\.?\d+(?:[eE][-+]?\d+)?)', reward_line)
                if m_reward:
                    try:
                        server_reward = float(m_reward.group(1))
                    except ValueError:
                        server_reward = None
                else:
                    server_reward = None

                episodes[episode] = (server_steps, server_reward)
            i += 5  # Skip past the summary block
        else:
            i += 1

    return episodes, error_count

def compare_episodes_and_write_csv(client_eps, server_eps, error_count, run_number, out_csv):
    """
    """
    total_step_mismatches = 0
    total_reward_mismatches = 0

    all_episodes = sorted(set(client_eps.keys()) | set(server_eps.keys()))
    with open(out_csv, 'a', newline='') as f:
        writer = csv.writer(f)
        f.seek(0)
        if os.stat(out_csv).st_size == 0:
            writer.writerow([
                "Run", "Episode", "ClientSteps", "ServerSteps", "StepsMismatch",
                "ClientReward", "ServerReward", "RewardMismatch", "ServerErrorCount"
            ])

        for ep in all_episodes:
            client_steps, client_reward = client_eps.get(ep, (None, None))
            server_steps, server_reward = server_eps.get(ep, (None, None))
            steps_mismatch = 0
            reward_mismatch = 0

            if client_steps is None or server_steps is None or client_steps != server_steps:
                steps_mismatch = 1
                total_step_mismatches += 1

            if (client_reward is None or server_reward is None or
                abs(client_reward - server_reward) > 1e-9):
                reward_mismatch = 1
                total_reward_mismatches += 1

            writer.writerow([
                run_number,
                ep,
                client_steps if client_steps is not None else "",
                server_steps if server_steps is not None else "",
                steps_mismatch,
                f"{client_reward:.6f}" if client_reward is not None else "",
                f"{server_reward:.6f}" if server_reward is not None else "",
                reward_mismatch,
                "" 
            ])
    print(f"Comparison complete. Results appended to {out_csv}")

def get_next_run_number(out_csv):
    run_number = 1
    if os.path.exists(out_csv):
        with open(out_csv, 'r', encoding='utf-8') as f:
            reader = csv.reader(f)
            max_run = 0
            for row in reader:
                if row and row[0].strip().isdigit():
                    try:
                        val = int(row[0].strip())
                        if val > max_run:
                            max_run = val
                    except ValueError:
                        pass
            run_number = max_run + 1
    return run_number

if __name__ == "__main__":
    client_log_file = input("Enter the full path to the client log file: ").strip()
    server_log_file = input("Enter the full path to the server log file: ").strip()
    output_csv_file = input("Enter the full path to the output CSV file: ").strip()
    run_number = get_next_run_number(output_csv_file)
    print(f"Next run number: {run_number}")
    client_episodes = parse_client_logs(client_log_file)
    server_episodes, error_count = parse_server_logs(server_log_file)
    compare_episodes_and_write_csv(client_episodes, server_episodes, error_count, run_number, output_csv_file)