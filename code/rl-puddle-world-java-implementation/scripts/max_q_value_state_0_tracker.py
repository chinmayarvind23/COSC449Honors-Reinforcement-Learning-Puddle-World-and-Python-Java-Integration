#!/usr/bin/env python3
import re
import csv
import sys
import os

def parse_client_logs_for_max_q(log_path):
    """
      - Check for "Updated Q-value for state 0, action X: ...", and pick max value at end of episode for state 0 and any action
      - Parse till "=== End of Episode Summary ===" appears
    """
    episodes = {}
    current_max = float('-inf')
    episode_num = 0
    qvalue_pattern = re.compile(r'^Updated Q-value for state 0, action \d+:\s+([-+]?\d*\.?\d+(?:[eE][-+]?\d+)?)')

    with open(log_path, 'r', encoding='utf-8') as f:
        for line in f:
            stripped = line.strip()
            match = qvalue_pattern.match(stripped)
            if match:
                try:
                    q_value = float(match.group(1))
                except ValueError:
                    q_value = float('-inf')
                if q_value > current_max:
                    current_max = q_value
            if stripped == "=== End of Episode Summary ===":
                episode_num += 1
                episodes[episode_num] = current_max if current_max != float('-inf') else None
                current_max = float('-inf')
    return episodes

def get_next_run_number(csv_path):
    run_number = 1
    if os.path.exists(csv_path):
        try:
            with open(csv_path, 'r', encoding='utf-8') as f:
                reader = csv.reader(f)
                max_run = 0
                for row in reader:
                    if row and row[0].strip().isdigit():
                        try:
                            val = int(row[0].strip())
                            if val > max_run:
                                max_run = val
                        except ValueError:
                            continue
                run_number = max_run + 1
        except Exception:
            run_number = 1
    return run_number

def append_max_q_to_csv(episodes, run_number, csv_path):
    file_empty = not os.path.exists(csv_path) or os.stat(csv_path).st_size == 0

    with open(csv_path, 'a', newline='') as f:
        writer = csv.writer(f)
        if file_empty:
            writer.writerow(["RunNumber", "EpisodeNumber", "MaxQValueForState0"])
        for ep in sorted(episodes.keys()):
            writer.writerow([run_number, ep, episodes[ep]])
    print(f"Appended run {run_number} results to {csv_path}")

def main():
    client_log_path = input("Enter the full path to the client log file: ").strip()
    output_csv_path = input("Enter the full path for the output CSV file: ").strip()
    run_number = get_next_run_number(output_csv_path)
    print(f"Next run number is: {run_number}")
    episodes = parse_client_logs_for_max_q(client_log_path)
    append_max_q_to_csv(episodes, run_number, output_csv_path)
if __name__ == "__main__":
    main()