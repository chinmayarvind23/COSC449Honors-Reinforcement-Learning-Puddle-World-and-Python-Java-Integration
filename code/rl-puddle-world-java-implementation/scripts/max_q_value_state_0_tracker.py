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

def parse_client_logs_for_max_q_diff(log_path):
    episodes = parse_client_logs_for_max_q(log_path)
    
    differences = {}
    prev_q = None
    for ep in sorted(episodes.keys()):
        current_q = episodes[ep]
        if prev_q is not None and current_q is not None:
            q_diff = current_q - prev_q
        else:
            q_diff = None
        
        differences[ep] = (current_q, q_diff)
        prev_q = current_q
    return differences

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
    
def append_max_q_to_csv(differences, run_number, csv_path):
    file_empty = not os.path.exists(csv_path) or os.stat(csv_path).st_size == 0

    with open(csv_path, 'a', newline='') as f:
        writer = csv.writer(f)
        if file_empty:
            writer.writerow(["RunNumber", "EpisodeNumber", "MaxQValueForState0", "MaxQDiff"])
        
        for ep in sorted(differences.keys()):
            max_q, q_diff = differences[ep]
            writer.writerow([run_number, ep, max_q, q_diff])
    
    print(f"Appended run {run_number} results (with Q-value differences) to {csv_path}")

def main():
    client_log_path = input("Enter path to client logs file: ").strip()
    output_csv_path = input("Enter path for output CSV file: ").strip()
    run_number = get_next_run_number(output_csv_path)
    print(f"Next run number is: {run_number}")
    differences = parse_client_logs_for_max_q_diff(client_log_path)
    append_max_q_to_csv(differences, run_number, output_csv_path)

if __name__ == "__main__":
    main()